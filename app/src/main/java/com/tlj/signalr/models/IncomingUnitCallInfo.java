package com.tlj.signalr.models;

import java.net.URL;

public class IncomingUnitCallInfo {
    private String callId;
    private String callerName;
    private EntranceViewModel entrance;
    private SiteInfoViewModel site;
    private URL callerPhotoUrl;

    // Constructor
    public IncomingUnitCallInfo(String callId, String callerName, EntranceViewModel entrance, SiteInfoViewModel site, URL callerPhotoUrl) {
        this.callId = callId;
        this.callerName = callerName;
        this.entrance = entrance;
        this.site = site;
        this.callerPhotoUrl = callerPhotoUrl;
    }

    // Getters
    public String getCallId() {
        return callId;
    }

    public String getCallerName() {
        return callerName;
    }

    public EntranceViewModel getEntrance() {
        return entrance;
    }

    public SiteInfoViewModel getSite() {
        return site;
    }

    public URL getCallerPhotoUrl() {
        return callerPhotoUrl;
    }
}

