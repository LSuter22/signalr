package com.tlj.signalr.models;

public class MissedCall {
    private String entrance;
    private String site;
    private String when;

    // Constructor
    public MissedCall(String entrance, String site, String when) {
        this.entrance = entrance;
        this.site = site;
        this.when = when;
    }

    // Getters
    public String getEntrance() {
        return entrance;
    }

    public String getSite() {
        return site;
    }

    public String getWhen() {
        return when;
    }
}

