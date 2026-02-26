package com.example.armariovirtual.models;

import java.io.Serializable;
import java.util.List;

public class Cloth implements Serializable {
    private String id;
    private String userId;
    private String name;
    private String descripcion;
    private String categoria;
    private String marca;
    private float precio;
    private String talla;
    private String temporada;
    private String imageUrl;
    private List<String> tags;
    private long timestamp;

    public Cloth() {
    }

    public Cloth(String id, String userId, String name, String descripcion, String categoria, String marca, float precio, String talla, String temporada, String imageUrl, List<String> tags, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.marca = marca;
        this.precio = precio;
        this.talla = talla;
        this.temporada = temporada;
        this.imageUrl = imageUrl;
        this.tags = tags;
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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public float getPrecio() {
        return precio;
    }

    public void setPrecio(float precio) {
        this.precio = precio;
    }

    public String getTalla() {
        return talla;
    }

    public void setTalla(String talla) {
        this.talla = talla;
    }

    public String getTemporada() {
        return temporada;
    }

    public void setTemporada(String temporada) {
        this.temporada = temporada;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
