package com.project.broker.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BrokerData {

    private Set<String> topics = new HashSet<>();
    private Map<String, Set<String>> messages = new HashMap<>();
    private Map<String, Set<String>> subscribers = new HashMap<>();

    public Set<String> getTopics() {
        return topics;
    }

    public void setTopics(Set<String> topics) {
        this.topics = topics;
    }

    public Map<String, Set<String>> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, Set<String>> messages) {
        this.messages = messages;
    }

    public Map<String, Set<String>> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(Map<String, Set<String>> subscribers) {
        this.subscribers = subscribers;
    }
} 