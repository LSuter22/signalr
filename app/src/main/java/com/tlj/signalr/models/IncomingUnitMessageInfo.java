package com.tlj.signalr.models;

public class IncomingUnitMessageInfo {
    private String callId;
    private String message;
    private EntranceViewModel entrance;
    private SiteInfoViewModel site;

    // Constructor
    public IncomingUnitMessageInfo(String callId, String message, EntranceViewModel entrance, SiteInfoViewModel site) {
        this.callId = callId;
        this.message = message;
        this.entrance = entrance;
        this.site = site;
    }

    // Getters
    public String getCallId() {
        return callId;
    }

    public String getMessage() {
        return message;
    }

    public EntranceViewModel getEntrance() {
        return entrance;
    }

    public SiteInfoViewModel getSite() {
        return site;
    }
}
