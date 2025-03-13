package com.project.subscriber.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SubscriberService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriberService.class);

    private final RestTemplate restTemplate;
    private final AdminClient adminClient;
    private final KafkaListenerEndpointRegistry kafkaListenerRegistry;

    @Value("${coordinator.url}")
    private String coordinatorUrl;

    @Value("${server.port}")
    private int port;

    private String leaderBroker;
    private List<String> subscribedTopics = new ArrayList<>();
    private Map<String, List<String>> topicMessages = new ConcurrentHashMap<>();
    private long logicalClock = 0;

    public SubscriberService(RestTemplate restTemplate, 
                            AdminClient adminClient,
                            KafkaListenerEndpointRegistry kafkaListenerRegistry) {
        this.restTemplate = restTemplate;
        this.adminClient = adminClient;
        this.kafkaListenerRegistry = kafkaListenerRegistry;
    }

    @PostConstruct
    public void init() {
        updateLeaderBroker();
        // Initialize the Kafka listener
        logger.info("Initializing Kafka listener for all topics");
    }

    @Scheduled(fixedRate = 5000)
    public void updateLeaderBroker() {
        incrementClock();
        try {
            this.leaderBroker = restTemplate.getForObject(coordinatorUrl + "/api/leader?timestamp=" + logicalClock, String.class);
            logger.info("Updated leader broker: {}", leaderBroker);
        } catch (Exception e) {
            logger.error("Error updating leader broker", e);
        }
    }

    // Listen to all topics
    @KafkaListener(id = "all-topics-listener", topicPattern = ".*")
    public void listen(ConsumerRecord<String, String> record) {
        String topic = record.topic();
        String message = record.value();
        
        // Skip internal Kafka topics
        if (topic.startsWith("__")) {
            return;
        }
        
        logger.info("Received message from topic {}: {}", topic, message);
        logger.info("Message details - Partition: {}, Offset: {}, Timestamp: {}", 
                   record.partition(), record.offset(), record.timestamp());
        
        // Store the message even if we haven't explicitly subscribed
        // This ensures we capture all messages
        topicMessages.computeIfAbsent(topic, k -> new ArrayList<>()).add(message);
        
        // If we receive a message for a topic we're not subscribed to,
        // automatically add it to our subscribed topics
        if (!subscribedTopics.contains(topic)) {
            subscribedTopics.add(topic);
            logger.info("Auto-subscribed to topic: {}", topic);
        }
    }

    public void subscribeTopic(String topic) {
        incrementClock();
        if (!subscribedTopics.contains(topic)) {
            subscribedTopics.add(topic);
            // Initialize the message list for this topic if it doesn't exist
            topicMessages.putIfAbsent(topic, new ArrayList<>());
            logger.info("Subscribed to topic: {}", topic);
        }
    }

    public void unsubscribeTopic(String topic) {
        incrementClock();
        subscribedTopics.remove(topic);
        logger.info("Unsubscribed from topic: {}", topic);
    }

    @Scheduled(fixedRate = 5000)
    public void syncTopics() {
        incrementClock();
        try {
            ListTopicsResult listTopicsResult = adminClient.listTopics();
            Set<String> kafkaTopics = listTopicsResult.names().get();
            // Remove internal Kafka topics
            kafkaTopics.removeIf(topic -> topic.startsWith("__"));
            logger.info("Available Kafka topics: {}", kafkaTopics);
        } catch (Exception e) {
            logger.error("Error getting Kafka topics", e);
        }
    }

    public List<String> getTopics() {
        incrementClock();
        try {
            ListTopicsResult topics = adminClient.listTopics();
            Set<String> kafkaTopics = topics.names().get();
            // Filter out internal Kafka topics
            kafkaTopics.removeIf(topic -> topic.startsWith("__"));
            logger.info("Available Kafka topics: {}", kafkaTopics);
            return new ArrayList<>(kafkaTopics);
        } catch (Exception e) {
            logger.error("Error getting Kafka topics", e);
            return new ArrayList<>();
        }
    }

    public List<String> getSubscribedTopics() {
        incrementClock();
        return subscribedTopics;
    }

    public Map<String, List<String>> getTopicMessages() {
        incrementClock();
        return topicMessages;
    }

    public List<String> getMessagesForTopic(String topic) {
        incrementClock();
        logger.info("Getting messages for topic: {}, logical clock: {}", topic, logicalClock);
        
        // If we're asked for messages for a topic we're not subscribed to,
        // automatically subscribe to it
        if (!subscribedTopics.contains(topic)) {
            logger.info("Auto-subscribing to topic: {} as it was not in subscribed topics: {}", topic, subscribedTopics);
            subscribeTopic(topic);
        }
        
        List<String> messages = topicMessages.getOrDefault(topic, new ArrayList<>());
        logger.info("Retrieved {} messages for topic: {}", messages.size(), topic);
        
        // Add more detailed logging
        if (messages.isEmpty()) {
            logger.warn("No messages found for topic: {}. This could indicate a Kafka consumer issue.", topic);
            // Try to check if the topic exists in Kafka
            try {
                Set<String> topics = adminClient.listTopics().names().get();
                if (topics.contains(topic)) {
                    logger.info("Topic {} exists in Kafka but no messages were retrieved", topic);
                    // Try to manually fetch messages
                    List<String> manuallyFetchedMessages = manuallyFetchMessagesFromKafka(topic);
                    if (!manuallyFetchedMessages.isEmpty()) {
                        logger.info("Manually fetched {} messages for topic {}", manuallyFetchedMessages.size(), topic);
                        // Update our in-memory store
                        topicMessages.put(topic, manuallyFetchedMessages);
                        return manuallyFetchedMessages;
                    }
                } else {
                    logger.warn("Topic {} does not exist in Kafka", topic);
                }
            } catch (Exception e) {
                logger.error("Error checking if topic exists in Kafka", e);
            }
        } else {
            logger.debug("First message sample: {}", messages.get(0));
            if (messages.size() > 1) {
                logger.debug("Last message sample: {}", messages.get(messages.size() - 1));
            }
        }
        
        return messages;
    }
    
    /**
     * Manually fetch messages from Kafka for a specific topic
     * This is a fallback method in case the regular Kafka listener isn't working
     */
    private List<String> manuallyFetchMessagesFromKafka(String topic) {
        List<String> messages = new ArrayList<>();
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "manual-fetcher-" + System.currentTimeMillis());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));
            
            // Poll for records
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(5000));
            
            logger.info("Manually fetched {} records for topic {}", records.count(), topic);
            
            records.forEach(record -> {
                logger.info("Manual fetch - Received message from topic {}: {}", record.topic(), record.value());
                messages.add(record.value());
            });
            
            // Commit offsets
            consumer.commitSync();
        } catch (Exception e) {
            logger.error("Error manually fetching messages from Kafka", e);
        }
        
        return messages;
    }

    public String getCoordinatorUrl() {
        return coordinatorUrl;
    }

    public void setCoordinatorUrl(String coordinatorUrl) {
        this.coordinatorUrl = coordinatorUrl;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
        logger.info("Subscriber service port set to: {}", port);
    }

    public String getLeaderBroker() {
        incrementClock();
        return leaderBroker;
    }

    private synchronized void incrementClock() {
        logicalClock++;
    }

    public synchronized void updateClock(long receivedTimestamp) {
        logicalClock = Math.max(logicalClock, receivedTimestamp) + 1;
    }

    public synchronized long getLogicalClock() {
        return logicalClock;
    }

    /**
     * Force a refresh of messages for a specific topic by manually fetching from Kafka
     */
    public List<String> refreshMessagesForTopic(String topic) {
        incrementClock();
        logger.info("Forcing refresh of messages for topic: {}, logical clock: {}", topic, logicalClock);
        
        // Ensure we're subscribed to the topic
        if (!subscribedTopics.contains(topic)) {
            logger.info("Auto-subscribing to topic: {} during refresh", topic);
            subscribeTopic(topic);
        }
        
        // Manually fetch messages from Kafka
        List<String> refreshedMessages = manuallyFetchMessagesFromKafka(topic);
        
        if (!refreshedMessages.isEmpty()) {
            logger.info("Refreshed {} messages for topic {}", refreshedMessages.size(), topic);
            // Update our in-memory store
            topicMessages.put(topic, refreshedMessages);
        } else {
            logger.warn("No messages found during refresh for topic: {}", topic);
        }
        
        return refreshedMessages;
    }
} 