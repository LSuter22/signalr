package com.tlj.signalr.models;

public class EntranceViewModel {
    private String id;
    private String name;
    private String siteId;

    // Constructor
    public EntranceViewModel(String id, String name, String siteId) {
        this.id = id;
        this.name = name;
        this.siteId = siteId;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSiteId() {
        return siteId;
    }
}
