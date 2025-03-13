package com.project.coordinator.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CoordinatorService {

    private final RestTemplate restTemplate;
    private List<String> brokers = new ArrayList<>();
    private Map<String, Long> lastHeartbeats = new HashMap<>();
    private String leader;
    private long logicalClock = 0;
    private final Pattern portPattern = Pattern.compile(":(\\d+)");
    
    // Track brokers that need leader notification retries
    private final Map<String, Integer> pendingLeaderNotifications = new ConcurrentHashMap<>();
    private static final int MAX_NOTIFICATION_RETRIES = 3;

    public CoordinatorService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<String> getBrokers() {
        incrementClock();
        return brokers;
    }

    public String getLeader() {
        incrementClock();
        return leader;
    }

    /**
     * Extracts the port number from a broker URL
     * @param brokerUrl URL in format http://hostname:port
     * @return port number as integer, or -1 if not found
     */
    private int extractPort(String brokerUrl) {
        Matcher matcher = portPattern.matcher(brokerUrl);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1;
    }

    public void registerBroker(String brokerUrl) {
        incrementClock();
        if (!brokers.contains(brokerUrl)) {
            brokers.add(brokerUrl);
            lastHeartbeats.put(brokerUrl, System.currentTimeMillis());
            System.out.println("Registered broker: " + brokerUrl);
            
            // Run leader election when a new broker joins
            electNewLeader();
        }
    }

    public void heartbeat(String brokerUrl) {
        incrementClock();
        lastHeartbeats.put(brokerUrl, System.currentTimeMillis());
        if (!brokers.contains(brokerUrl)) {
            brokers.add(brokerUrl);
            System.out.println("Added broker from heartbeat: " + brokerUrl);
            
            // Run leader election when a new broker is discovered
            electNewLeader();
        }
    }

    @Scheduled(fixedRate = 5000)
    public void checkBrokerStatus() {
        incrementClock();
        long now = System.currentTimeMillis();
        List<String> deadBrokers = new ArrayList<>();

        for (Map.Entry<String, Long> entry : lastHeartbeats.entrySet()) {
            if (now - entry.getValue() > 10000) {  // 10 seconds timeout
                deadBrokers.add(entry.getKey());
            }
        }

        boolean leaderDied = false;
        for (String deadBroker : deadBrokers) {
            brokers.remove(deadBroker);
            lastHeartbeats.remove(deadBroker);
            System.out.println("Removed dead broker: " + deadBroker);
            
            if (leader != null && leader.equals(deadBroker)) {
                leaderDied = true;
            }
        }

        if (leaderDied) {
            electNewLeader();
        }
    }

    /**
     * Implements the Bully Algorithm for leader election
     * Selects the broker with the highest port number as the leader
     */
    private void electNewLeader() {
        incrementClock();
        if (brokers.isEmpty()) {
            leader = null;
            System.out.println("No brokers available, leader set to null");
            return;
        }
        
        // Find the broker with the highest port number (Bully Algorithm)
        String highestPortBroker = null;
        int highestPort = -1;
        
        for (String broker : brokers) {
            int port = extractPort(broker);
            if (port > highestPort) {
                highestPort = port;
                highestPortBroker = broker;
            }
        }
        
        // Set the new leader
        leader = highestPortBroker;
        System.out.println("Elected new leader using Bully Algorithm: " + leader + " with port " + highestPort);
        
        // Notify all brokers about the new leader
        notifyBrokersAboutNewLeader();
    }

    /**
     * Notifies all brokers about the new leader
     */
    private void notifyBrokersAboutNewLeader() {
        if (leader == null) return;
        
        for (String broker : brokers) {
            try {
                restTemplate.postForObject(
                    broker + "/api/leader-changed?timestamp=" + logicalClock,
                    leader,
                    String.class
                );
                System.out.println("Notified broker " + broker + " about new leader: " + leader);
                // Remove from pending notifications if successful
                pendingLeaderNotifications.remove(broker);
            } catch (RestClientException e) {
                System.err.println("Failed to notify broker " + broker + " about new leader: " + e.getMessage());
                // Add to pending notifications for retry
                pendingLeaderNotifications.put(broker, 0);
            }
        }
    }
    
    /**
     * Scheduled task to retry leader notifications for brokers that weren't ready
     */
    @Scheduled(fixedRate = 2000)
    public void retryLeaderNotifications() {
        if (leader == null || pendingLeaderNotifications.isEmpty()) return;
        
        Iterator<Map.Entry<String, Integer>> iterator = pendingLeaderNotifications.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            String broker = entry.getKey();
            int retryCount = entry.getValue();
            
            if (retryCount >= MAX_NOTIFICATION_RETRIES) {
                System.out.println("Max retries reached for broker " + broker + ". Giving up on leader notification.");
                iterator.remove();
                continue;
            }
            
            try {
                restTemplate.postForObject(
                    broker + "/api/leader-changed?timestamp=" + logicalClock,
                    leader,
                    String.class
                );
                System.out.println("Successfully notified broker " + broker + " about leader after retry");
                iterator.remove();
            } catch (Exception e) {
                System.err.println("Retry failed to notify broker " + broker + " about leader: " + e.getMessage());
                pendingLeaderNotifications.put(broker, retryCount + 1);
            }
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