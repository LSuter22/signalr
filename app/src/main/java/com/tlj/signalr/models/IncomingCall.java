package com.tlj.signalr.models;

public class IncomingCall {
    private static final IncomingCall shared = new IncomingCall();

    private IncomingCall() {
        // Private constructor to enforce singleton pattern
    }

    public static IncomingCall getShared() {
        return shared;
    }
}
