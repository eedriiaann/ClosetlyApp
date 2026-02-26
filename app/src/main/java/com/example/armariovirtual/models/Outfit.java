package com.example.armariovirtual.models;

import java.util.Map;

public class Outfit {
    private String id;
    private String userId;
    private Map<String, String> slots;
    private long timestamp;

    public Outfit() {
    }

    public Outfit(String id, String userId, Map<String, String> slots, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.slots = slots;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, String> getSlots() {
        return slots;
    }

    public void setSlots(Map<String, String> slots) {
        this.slots = slots;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}