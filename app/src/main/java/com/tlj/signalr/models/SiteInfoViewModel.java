package com.tlj.signalr.models;

public class SiteInfoViewModel {
    private String id;
    private String name;

    // Constructor
    public SiteInfoViewModel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
