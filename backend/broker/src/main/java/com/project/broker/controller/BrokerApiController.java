package com.project.broker.controller;

import com.project.broker.service.BrokerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class BrokerApiController {

    @Autowired
    private BrokerService brokerService;

    @GetMapping("/brokers")
    public List<String> getBrokers(@RequestParam long timestamp) {
        brokerService.updateClock(timestamp);
        return brokerService.getBrokers();
    }

    @GetMapping("/leader")
    public String getLeader(@RequestParam long timestamp) {
        brokerService.updateClock(timestamp);
        return brokerService.getLeader();
    }

    @GetMapping("/topics")
    public Set<String> getTopics(@RequestParam long timestamp) {
        brokerService.updateClock(timestamp);
        return brokerService.getTopics();
    }

    @GetMapping("/messages")
    public List<String> getMessages(@RequestParam String topic, @RequestParam String subscriberUrl, @RequestParam long timestamp) {
        brokerService.updateClock(timestamp);
        if (!brokerService.isSubscriberSubscribedToTopic(subscriberUrl, topic)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Subscriber is not subscribed to this topic");
        }
        return brokerService.getMessages(topic);
    }

    @GetMapping("/data")
    public Map<String, Object> getAllData(@RequestParam long timestamp) {
        brokerService.updateClock(timestamp);
        return brokerService.getAllData();
    }

    @PostMapping("/add-topic")
    public void addTopic(@RequestBody String topic, @RequestParam long timestamp) {
        brokerService.updateClock(timestamp);
        brokerService.addTopic(topic);
    }

    @PostMapping("/add-message")
    public void addMessage(@RequestParam String topic, @RequestBody String message, @RequestParam long timestamp) {
        brokerService.updateClock(timestamp);
        brokerService.addMessage(topic, message);
    }

    @PostMapping("/add-subscriber")
    public void addSubscriber(@RequestParam String topic, @RequestBody String subscriberUrl, @RequestParam long timestamp) {
        brokerService.updateClock(timestamp);
        brokerService.addSubscriber(topic, subscriberUrl);
    }

    @PostMapping("/remove-subscriber")
    public void removeSubscriber(@RequestParam String topic, @RequestBody String subscriberUrl, @RequestParam long timestamp) {
        brokerService.updateClock(timestamp);
        brokerService.removeSubscriber(topic, subscriberUrl);
    }

    @GetMapping("/ping")
    public String ping(@RequestParam long timestamp) {
        brokerService.updateClock(timestamp);
        return "pong";
    }

    @GetMapping("/subscribers")
    public Map<String, List<String>> getSubscribers(@RequestParam long timestamp) {
        brokerService.updateClock(timestamp);
        return brokerService.getSubscribersWithTopics();
    }
    
    /**
     * Endpoint to handle leader-changed notifications from the coordinator
     * This is part of the Bully Algorithm implementation
     */
    @PostMapping("/leader-changed")
    public void leaderChanged(@RequestBody String newLeader, @RequestParam long timestamp) {
        brokerService.updateClock(timestamp);
        
        // Check if the broker is ready to receive messages
        if (!brokerService.isReadyToReceiveMessages()) {
            System.out.println("WARNING: Received leader-changed notification before broker was ready. Will process anyway.");
        }
        
        brokerService.setLeader(newLeader);
        System.out.println("Received leader-changed notification. New leader: " + newLeader);
    }
} 