package com.project.subscriber.controller;

import com.project.subscriber.service.SubscriberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SubscriberThymeleafController {

    @Autowired
    private SubscriberService subscriberService;

    @GetMapping("/")
    public String home(Model model, @RequestParam(required = false, defaultValue = "0") long timestamp) {
        subscriberService.updateClock(timestamp);
        model.addAttribute("subscriberUrl", "http://localhost:" + subscriberService.getPort());
        model.addAttribute("leaderBroker", subscriberService.getLeaderBroker());
        model.addAttribute("topics", subscriberService.getTopics());
        model.addAttribute("subscribedTopics", subscriberService.getSubscribedTopics());
        model.addAttribute("topicMessages", subscriberService.getTopicMessages());
        model.addAttribute("timestamp", subscriberService.getLogicalClock());
        return "subscriber";
    }

    @GetMapping("/subscribe")
    public String subscribeForm(Model model, @RequestParam(required = false, defaultValue = "0") long timestamp) {
        subscriberService.updateClock(timestamp);
        model.addAttribute("topics", subscriberService.getTopics());
        model.addAttribute("subscribedTopics", subscriberService.getSubscribedTopics());
        model.addAttribute("timestamp", subscriberService.getLogicalClock());
        return "subscribe";
    }

    @PostMapping("/subscribe")
    public String subscribe(@RequestParam String topic, @RequestParam(required = false, defaultValue = "0") long timestamp) {
        subscriberService.updateClock(timestamp);
        subscriberService.subscribeTopic(topic);
        return "redirect:/?timestamp=" + subscriberService.getLogicalClock();
    }

    @GetMapping("/unsubscribe")
    public String unsubscribeForm(Model model, @RequestParam(required = false, defaultValue = "0") long timestamp) {
        subscriberService.updateClock(timestamp);
        model.addAttribute("subscribedTopics", subscriberService.getSubscribedTopics());
        model.addAttribute("timestamp", subscriberService.getLogicalClock());
        return "unsubscribe";
    }

    @PostMapping("/unsubscribe")
    public String unsubscribe(@RequestParam String topic, @RequestParam(required = false, defaultValue = "0") long timestamp) {
        subscriberService.updateClock(timestamp);
        subscriberService.unsubscribeTopic(topic);
        return "redirect:/?timestamp=" + subscriberService.getLogicalClock();
    }

    @GetMapping("/messages")
    public String messages(Model model, @RequestParam(required = false, defaultValue = "0") long timestamp) {
        subscriberService.updateClock(timestamp);
        model.addAttribute("topicMessages", subscriberService.getTopicMessages());
        model.addAttribute("timestamp", subscriberService.getLogicalClock());
        return "messages";
    }

    @GetMapping("/status")
    public String status(Model model, @RequestParam(required = false, defaultValue = "0") long timestamp) {
        subscriberService.updateClock(timestamp);
        model.addAttribute("subscriberUrl", "http://localhost:" + subscriberService.getPort());
        model.addAttribute("leaderBroker", subscriberService.getLeaderBroker());
        model.addAttribute("subscribedTopics", subscriberService.getSubscribedTopics());
        model.addAttribute("timestamp", subscriberService.getLogicalClock());
        return "status";
    }
} 