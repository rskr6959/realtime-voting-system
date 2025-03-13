package com.project.publisher.controller;

import com.project.publisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PublisherThymeleafController {

    @Autowired
    private PublisherService publisherService;

    @GetMapping("/")
    public String home(Model model, @RequestParam(required = false, defaultValue = "0") long timestamp) {
        publisherService.updateClock(timestamp);
        model.addAttribute("publisherUrl", "http://localhost:" + publisherService.getPort());
        model.addAttribute("leaderBroker", publisherService.getLeaderBroker());
        model.addAttribute("timestamp", publisherService.getLogicalClock());
        return "publisher";
    }

    @GetMapping("/publish")
    public String publishForm(Model model, @RequestParam(required = false, defaultValue = "0") long timestamp) {
        publisherService.updateClock(timestamp);
        model.addAttribute("timestamp", publisherService.getLogicalClock());
        return "publish";
    }

    @PostMapping("/publish")
    public String publish(@RequestParam String topic, @RequestParam String message, @RequestParam(required = false, defaultValue = "0") long timestamp) {
        publisherService.updateClock(timestamp);
        publisherService.publishMessage(topic, message);
        return "redirect:/?timestamp=" + publisherService.getLogicalClock();
    }

    @GetMapping("/status")
    public String status(Model model, @RequestParam(required = false, defaultValue = "0") long timestamp) {
        publisherService.updateClock(timestamp);
        model.addAttribute("publisherUrl", "http://localhost:" + publisherService.getPort());
        model.addAttribute("leaderBroker", publisherService.getLeaderBroker());
        model.addAttribute("timestamp", publisherService.getLogicalClock());
        return "status";
    }
} 