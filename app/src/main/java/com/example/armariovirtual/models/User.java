package com.example.armariovirtual.models;


public class User {
    private String id;
    private String username;
    private String name;
    private String profileImageUrl;

    public User() {
    }

    public User(String id, String username, String name, String profileImageUrl) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}