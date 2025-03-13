package com.project.broker.controller;

import com.project.broker.service.BrokerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class BrokerThymeleafController {

    @Autowired
    private BrokerService brokerService;

    @GetMapping("/")
    public String home(Model model, @RequestParam(required = false, defaultValue = "0") long timestamp) {
        brokerService.updateClock(timestamp);
        model.addAttribute("brokerUrl", "http://localhost:" + brokerService.getPort());
        model.addAttribute("leader", brokerService.getLeader());
        model.addAttribute("brokers", brokerService.getBrokers());
        model.addAttribute("topics", brokerService.getTopics());
        model.addAttribute("messages", brokerService.getMessages());
        model.addAttribute("subscribers", brokerService.getSubscribersWithTopics());
        model.addAttribute("isLeader", ("http://localhost:" + brokerService.getPort()).equals(brokerService.getLeader()));
        model.addAttribute("timestamp", brokerService.getLogicalClock());
        return "broker";
    }

    @GetMapping("/topics")
    public String topics(Model model, @RequestParam(required = false, defaultValue = "0") long timestamp) {
        brokerService.updateClock(timestamp);
        model.addAttribute("topics", brokerService.getTopics());
        model.addAttribute("timestamp", brokerService.getLogicalClock());
        return "topics";
    }

    @GetMapping("/messages")
    public String messages(Model model, @RequestParam(required = false, defaultValue = "0") long timestamp) {
        brokerService.updateClock(timestamp);
        model.addAttribute("messages", brokerService.getMessages());
        model.addAttribute("timestamp", brokerService.getLogicalClock());
        return "messages";
    }

    @GetMapping("/subscribers")
    public String subscribers(Model model, @RequestParam(required = false, defaultValue = "0") long timestamp) {
        brokerService.updateClock(timestamp);
        model.addAttribute("subscribers", brokerService.getSubscribersWithTopics());
        model.addAttribute("timestamp", brokerService.getLogicalClock());
        return "subscribers";
    }

    @GetMapping("/status")
    public String status(Model model, @RequestParam(required = false, defaultValue = "0") long timestamp) {
        brokerService.updateClock(timestamp);
        model.addAttribute("brokerUrl", "http://localhost:" + brokerService.getPort());
        model.addAttribute("leader", brokerService.getLeader());
        model.addAttribute("brokers", brokerService.getBrokers());
        model.addAttribute("isLeader", ("http://localhost:" + brokerService.getPort()).equals(brokerService.getLeader()));
        model.addAttribute("timestamp", brokerService.getLogicalClock());
        return "status";
    }
} 