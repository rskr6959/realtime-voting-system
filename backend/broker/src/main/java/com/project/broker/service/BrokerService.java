package com.project.broker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class BrokerService {

    private final RestTemplate restTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AdminClient adminClient;
    private final ConsumerFactory<String, String> consumerFactory;

    @Value("${coordinator.url}")
    private String coordinatorUrl;

    @Value("${server.port}")
    private int port;

    private String leader;
    private List<String> brokers = new ArrayList<>();
    private Set<String> topics = new HashSet<>();
    private Map<String, List<String>> messages = new HashMap<>();
    private Map<String, List<String>> subscribers = new HashMap<>();
    private long logicalClock = 0;
    private final AtomicBoolean readyToReceiveMessages = new AtomicBoolean(false);
    private final AtomicBoolean heartbeatStarted = new AtomicBoolean(false);

    public BrokerService(RestTemplate restTemplate, 
                         KafkaTemplate<String, String> kafkaTemplate,
                         AdminClient adminClient,
                         ConsumerFactory<String, String> consumerFactory) {
        this.restTemplate = restTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.adminClient = adminClient;
        this.consumerFactory = consumerFactory;
    }

    @PostConstruct
    public void init() {
        // Mark as ready to receive messages first
        readyToReceiveMessages.set(true);
        System.out.println("Broker is now ready to receive messages");
        
        // Wait 3 seconds before starting heartbeats to ensure full initialization
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            System.out.println("Starting broker registration and heartbeat after delay");
            registerBroker();
            updateLeaderAndBrokers();
            heartbeatStarted.set(true);
            scheduler.shutdown();
        }, 3, TimeUnit.SECONDS);
    }

    /**
     * Sets the port for this broker
     * @param port the port number
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Sets the leader broker
     * This is called when the coordinator notifies this broker about a leader change
     * @param newLeader the URL of the new leader broker
     */
    public void setLeader(String newLeader) {
        this.leader = newLeader;
        System.out.println("Leader updated to: " + newLeader);
        
        // If this broker is the new leader, log it
        String thisUrl = "http://localhost:" + port;
        if (thisUrl.equals(newLeader)) {
            System.out.println("THIS BROKER IS NOW THE LEADER!");
        }
    }

    /**
     * Checks if the broker is ready to receive messages
     * @return true if the broker is ready, false otherwise
     */
    public boolean isReadyToReceiveMessages() {
        return readyToReceiveMessages.get();
    }

    public boolean isSubscriberAlive(String subscriberUrl) {
        try {
            restTemplate.getForObject(subscriberUrl + "/api/ping?timestamp=" + logicalClock, String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, List<String>> getSubscribersWithTopics() {
        incrementClock();
        return subscribers;
    }

    @Scheduled(fixedRate = 1000)
    public void sendHeartbeat() {
        // Only send heartbeats if the broker is ready and heartbeat has been started
        if (!readyToReceiveMessages.get() || !heartbeatStarted.get()) {
            return;
        }
        
        incrementClock();
        System.out.println("Sending heart beat: " + new Date());
        try {
            String brokerUrl = "http://localhost:" + port;
            restTemplate.postForObject(coordinatorUrl + "/api/heartbeat?timestamp=" + logicalClock, brokerUrl, String.class);
            System.out.println("Sent heartbeat from broker at: " + brokerUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 3000)
    public void updateLeaderAndBrokers() {
        incrementClock();
        try {
            this.leader = restTemplate.getForObject(coordinatorUrl + "/api/leader?timestamp=" + logicalClock, String.class);
            this.brokers = restTemplate.getForObject(coordinatorUrl + "/api/brokers?timestamp=" + logicalClock, List.class);
            System.out.println("Updated leader: " + leader);
            System.out.println("Updated brokers: " + brokers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 1000)
    public void syncDataWithLeader() {
        incrementClock();
        if (!("http://localhost:" + port).equals(leader)) {
            try {
                String leaderUrl = leader + "/api/data?timestamp=" + logicalClock;
                Map<String, Object> leaderData = restTemplate.getForObject(leaderUrl, Map.class);
                updateInMemoryData(leaderData);
                System.out.println("Synchronized data with leader broker at: " + leaderUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateInMemoryData(Map<String, Object> data) {
        this.topics = new HashSet<>((List<String>) data.get("topics"));
        this.messages = (Map<String, List<String>>) data.get("messages");
        this.subscribers = (Map<String, List<String>>) data.get("subscribers");
    }

    private void registerBroker() {
        incrementClock();
        if (port!=0) {
            try {
                String brokerUrl = "http://localhost:" + port;
                restTemplate.postForObject(coordinatorUrl + "/api/register?timestamp=" + logicalClock, brokerUrl, String.class);
                System.out.println("Registered broker at: " + brokerUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isSubscriberSubscribedToTopic(String subscriberUrl, String topic) {
        incrementClock();
        return subscribers.containsKey(topic) && subscribers.get(topic).contains(subscriberUrl);
    }

    public List<String> getBrokers() {
        incrementClock();
        return brokers;
    }

    public String getLeader() {
        incrementClock();
        return leader;
    }

    public Set<String> getTopics() {
        incrementClock();
        return topics;
    }

    public Map<String, List<String>> getMessages() {
        incrementClock();
        return messages;
    }

    public List<String> getMessages(String topic) {
        incrementClock();
        return messages.getOrDefault(topic, new ArrayList<>());
    }

    public void addTopic(String topic) {
        incrementClock();
        try {
            // Create a new Kafka topic
            NewTopic newTopic = new NewTopic(topic, 1, (short) 1);
            adminClient.createTopics(Collections.singleton(newTopic));
            topics.add(topic);
            System.out.println("Created Kafka topic: " + topic);
        } catch (Exception e) {
            System.out.println("Error creating Kafka topic: " + e.getMessage());
        }
    }

    public void addMessage(String topic, String message) {
        incrementClock();
        try {
            // Send message to Kafka topic
            kafkaTemplate.send(topic, message);
            System.out.println("Message sent to Kafka topic " + topic + ": " + message);
        } catch (Exception e) {
            System.out.println("Error sending message to Kafka: " + e.getMessage());
        }
    }

    public List<String> getSubscribers() {
        incrementClock();
        List<String> allSubscribers = new ArrayList<>();
        for (List<String> topicSubscribers : subscribers.values()) {
            allSubscribers.addAll(topicSubscribers);
        }
        return allSubscribers;
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

    public void logNewPublisherContact(String publisherUrl) {
        incrementClock();
        System.out.println("New publisher contacted the leader broker: " + publisherUrl);
    }

    public void addSubscriber(String topic, String subscriberUrl) {
        incrementClock();
        subscribers.computeIfAbsent(topic, k -> new ArrayList<>()).add(subscriberUrl);
    }

    public void removeSubscriber(String topic, String subscriberUrl) {
        incrementClock();
        List<String> topicSubscribers = subscribers.get(topic);
        if (topicSubscribers != null) {
            topicSubscribers.remove(subscriberUrl);
            if (topicSubscribers.isEmpty()) {
                subscribers.remove(topic);
            }
        }
    }

    public List<String> getSubscribers(String topic) {
        incrementClock();
        return subscribers.getOrDefault(topic, new ArrayList<>());
    }

    public Map<String, Object> getAllData() {
        incrementClock();
        Map<String, Object> data = new HashMap<>();
        data.put("topics", new ArrayList<>(topics));
        data.put("messages", new HashMap<>(messages));
        data.put("subscribers", new HashMap<>(subscribers));
        return data;
    }

    @Scheduled(fixedRate = 5000)
    public void syncTopicsWithKafka() {
        incrementClock();
        try {
            ListTopicsResult listTopicsResult = adminClient.listTopics();
            Set<String> kafkaTopics = listTopicsResult.names().get();
            // Remove internal Kafka topics
            kafkaTopics.removeIf(topic -> topic.startsWith("__"));
            this.topics = new HashSet<>(kafkaTopics);
            System.out.println("Synced topics with Kafka: " + topics);
        } catch (Exception e) {
            System.out.println("Error syncing topics with Kafka: " + e.getMessage());
        }
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
} 