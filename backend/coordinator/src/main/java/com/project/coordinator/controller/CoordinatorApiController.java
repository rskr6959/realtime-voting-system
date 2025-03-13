package com.project.coordinator.controller;

import com.project.coordinator.service.CoordinatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CoordinatorApiController {

    @Autowired
    private CoordinatorService coordinatorService;

    @GetMapping("/brokers")
    public List<String> getBrokers(@RequestParam long timestamp) {
        coordinatorService.updateClock(timestamp);
        return coordinatorService.getBrokers();
    }

    @GetMapping("/leader")
    public String getLeader(@RequestParam long timestamp) {
        coordinatorService.updateClock(timestamp);
        return coordinatorService.getLeader();
    }

    @PostMapping("/register")
    public void registerBroker(@RequestBody String brokerUrl, @RequestParam long timestamp) {
        coordinatorService.updateClock(timestamp);
        coordinatorService.registerBroker(brokerUrl);
    }

    @PostMapping("/heartbeat")
    public void heartbeat(@RequestBody String brokerUrl, @RequestParam long timestamp) {
        coordinatorService.updateClock(timestamp);
        coordinatorService.heartbeat(brokerUrl);
    }

    @GetMapping("/ping")
    public String ping(@RequestParam long timestamp) {
        coordinatorService.updateClock(timestamp);
        return "pong";
    }
} 