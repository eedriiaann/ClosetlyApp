package com.example.armariovirtual.models;

import java.util.List;

public class Publication {
    private String id;
    private String userId;
    private String name;
    private String descripcion;
    private int megusta;
    private String imageUrl;
    private String pfpUrl;
    private long timestamp;
    private List<String> clothIds;

    public Publication() {
    }

    public Publication(String id, String userId, String name, String descripcion, int megusta, String imageUrl, String pfpUrl, long timestamp, List<String> clothIds) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.descripcion = descripcion;
        this.megusta = megusta;
        this.imageUrl = imageUrl;
        this.pfpUrl = pfpUrl;
        this.timestamp = timestamp;
        this.clothIds = clothIds;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getMegusta() {
        return megusta;
    }

    public void setMegusta(int megusta) {
        this.megusta = megusta;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPfpUrl() {
        return pfpUrl;
    }

    public void setPfpUrl(String pfpUrl) {
        this.pfpUrl = pfpUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getClothIds() {
        return clothIds;
    }

}

