package com.project.publisher.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Service
public class PublisherService {

    private final RestTemplate restTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AdminClient adminClient;

    @Value("${coordinator.url}")
    private String coordinatorUrl;

    @Value("${server.port}")
    private int port;

    private String leaderBroker;
    private List<String> topics = new ArrayList<>();
    private long logicalClock = 0;

    public PublisherService(RestTemplate restTemplate, 
                           KafkaTemplate<String, String> kafkaTemplate,
                           AdminClient adminClient) {
        this.restTemplate = restTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.adminClient = adminClient;
    }

    @PostConstruct
    public void init() {
        updateLeaderBroker();
        createTopic("Candidate_1");
        createTopic("Candidate_2");
        syncTopics();
    }

    @Scheduled(fixedRate = 5000)
    public void updateLeaderBroker() {
        incrementClock();
        try {
            this.leaderBroker = restTemplate.getForObject(coordinatorUrl + "/api/leader?timestamp=" + logicalClock, String.class);
            System.out.println("Updated leader broker: " + leaderBroker);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 3000)
    public void syncTopics() {
        incrementClock();
        try {
            ListTopicsResult listTopicsResult = adminClient.listTopics();
            Set<String> kafkaTopics = listTopicsResult.names().get();
            // Remove internal Kafka topics
            kafkaTopics.removeIf(topic -> topic.startsWith("__"));
            this.topics = new ArrayList<>(kafkaTopics);
            System.out.println("Synced topics with Kafka: " + topics);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void publishMessage(String topic, String message) {
        incrementClock();
        try {
            // Send message to Kafka topic
            kafkaTemplate.send(topic, message);
            System.out.println("Published message to Kafka topic " + topic + ": " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public List<String> getTopics() {
        return topics;
    }

    private void createTopic(String topicName) {
        try {
            NewTopic newTopic = new NewTopic(topicName, 1, (short) 1);
            adminClient.createTopics(Collections.singleton(newTopic)).all().get();
            System.out.println("Created Kafka topic: " + topicName);
            syncTopics();
        } catch (InterruptedException | ExecutionException e) {
            // Topic might already exist, which is fine
            System.out.println("Note: Topic " + topicName + " may already exist");
        }
    }
} 