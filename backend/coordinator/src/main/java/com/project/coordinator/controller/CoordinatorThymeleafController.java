package com.project.coordinator.controller;

import com.project.coordinator.service.CoordinatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CoordinatorThymeleafController {

    @Autowired
    private CoordinatorService coordinatorService;

    @GetMapping("/")
    public String home(Model model, @RequestParam(required = false, defaultValue = "0") long timestamp) {
        coordinatorService.updateClock(timestamp);
        model.addAttribute("brokers", coordinatorService.getBrokers());
        model.addAttribute("leader", coordinatorService.getLeader());
        model.addAttribute("timestamp", coordinatorService.getLogicalClock());
        return "coordinator";
    }

    @GetMapping("/brokers")
    public String brokers(Model model, @RequestParam(required = false, defaultValue = "0") long timestamp) {
        coordinatorService.updateClock(timestamp);
        model.addAttribute("brokers", coordinatorService.getBrokers());
        model.addAttribute("timestamp", coordinatorService.getLogicalClock());
        return "brokers";
    }

    @GetMapping("/leader")
    public String leader(Model model, @RequestParam(required = false, defaultValue = "0") long timestamp) {
        coordinatorService.updateClock(timestamp);
        model.addAttribute("leader", coordinatorService.getLeader());
        model.addAttribute("timestamp", coordinatorService.getLogicalClock());
        return "leader";
    }
} 