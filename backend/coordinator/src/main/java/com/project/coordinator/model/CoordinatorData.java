package com.project.coordinator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoordinatorData {
    private List<String> brokers = new ArrayList<>();
    private Map<String, Long> lastHeartbeats = new HashMap<>();
    private String leader;

    public List<String> getBrokers() {
        return brokers;
    }

    public void setBrokers(List<String> brokers) {
        this.brokers = brokers;
    }

    public Map<String, Long> getLastHeartbeats() {
        return lastHeartbeats;
    }

    public void setLastHeartbeats(Map<String, Long> lastHeartbeats) {
        this.lastHeartbeats = lastHeartbeats;
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }
} 