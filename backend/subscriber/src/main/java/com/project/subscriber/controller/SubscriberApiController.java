package com.project.subscriber.controller;

import com.project.subscriber.service.SubscriberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SubscriberApiController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriberApiController.class);

    @Autowired
    private SubscriberService subscriberService;

    @GetMapping("/topics")
    public List<String> getTopics(@RequestParam long timestamp) {
        subscriberService.updateClock(timestamp);
        return subscriberService.getTopics();
    }

    @GetMapping("/subscribed-topics")
    public List<String> getSubscribedTopics(@RequestParam long timestamp) {
        subscriberService.updateClock(timestamp);
        return subscriberService.getSubscribedTopics();
    }

    @GetMapping("/messages")
    public Map<String, List<String>> getMessages(@RequestParam long timestamp) {
        subscriberService.updateClock(timestamp);
        return subscriberService.getTopicMessages();
    }

    @GetMapping("/messages/{topic}")
    public List<String> getMessagesForTopic(@PathVariable String topic, @RequestParam long timestamp) {
        logger.info("Received GET RESULTS request for topic: {}, timestamp: {}", topic, timestamp);
        subscriberService.updateClock(timestamp);
        List<String> messages = subscriberService.getMessagesForTopic(topic);
        logger.info("Returning {} messages for topic: {}", messages.size(), topic);
        return messages;
    }

    @GetMapping("/refresh-messages/{topic}")
    public List<String> refreshMessagesForTopic(@PathVariable String topic, @RequestParam long timestamp) {
        logger.info("Received REFRESH MESSAGES request for topic: {}, timestamp: {}", topic, timestamp);
        subscriberService.updateClock(timestamp);
        // Force a manual refresh of messages
        List<String> messages = subscriberService.refreshMessagesForTopic(topic);
        logger.info("Returning {} refreshed messages for topic: {}", messages.size(), topic);
        return messages;
    }

    @PostMapping("/subscribe")
    public void subscribeTopic(@RequestBody String topic, @RequestParam long timestamp) {
        subscriberService.updateClock(timestamp);
        subscriberService.subscribeTopic(topic);
    }

    @PostMapping("/unsubscribe")
    public void unsubscribeTopic(@RequestBody String topic, @RequestParam long timestamp) {
        subscriberService.updateClock(timestamp);
        subscriberService.unsubscribeTopic(topic);
    }

    @GetMapping("/leader-broker")
    public String getLeaderBroker(@RequestParam long timestamp) {
        subscriberService.updateClock(timestamp);
        return subscriberService.getLeaderBroker();
    }

    @GetMapping("/ping")
    public String ping(@RequestParam long timestamp) {
        subscriberService.updateClock(timestamp);
        return "pong";
    }
} 