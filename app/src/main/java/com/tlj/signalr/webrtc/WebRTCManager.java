package com.tlj.signalr.webrtc;

import android.content.Context;
import android.util.Log;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.util.Collections;

public class WebRTCManager {
    private PeerConnectionFactory factory;
    private PeerConnection peerConnection;

    public WebRTCManager(Context context) {
        // Initialize PeerConnectionFactory
        PeerConnectionFactory.InitializationOptions initializationOptions = PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);
        factory = PeerConnectionFactory.builder().createPeerConnectionFactory();
    }

    public void startRTCConnection() {
        // Create configuration
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(Collections.singletonList(new PeerConnection.IceServer("stun:stun.l.google.com:19302")));

        // Create PeerConnection
        peerConnection = factory.createPeerConnection(rtcConfig, new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {

                if (signalingState == PeerConnection.SignalingState.STABLE){
                    Log.d("WebRTC", "SignalState Stable");
                } else if (signalingState == PeerConnection.SignalingState.CLOSED){
                    Log.d("WebRTC", "SignalState Closed");
                }else if (signalingState == PeerConnection.SignalingState.HAVE_LOCAL_OFFER){
                    Log.d("WebRTC", "SignalState Local offer");
                }else if (signalingState == PeerConnection.SignalingState.HAVE_LOCAL_PRANSWER){
                    Log.d("WebRTC", "SignalState Local Ans");
                } else if (signalingState == PeerConnection.SignalingState.HAVE_REMOTE_OFFER){
                    Log.d("WebRTC", "SignalState Remote Offer");
                } else if (signalingState == PeerConnection.SignalingState.HAVE_REMOTE_PRANSWER){
                    Log.d("WebRTC", "SignalState Remote Answer");
                }

            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Log.d("WebRTC", "ICE Connection");
            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {
                Log.d("WebRTC", "Local ICE candidate rec");
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                Log.d("WebRTC", "Local ICE candidate gather change");

            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                Log.d("WebRTC", "Local ICE candidate generated");
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                Log.d("WebRTC", "Local ICE candidate remove");

            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                Log.d("WebRTC", "Local ICE candidate stream 1");

            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
                Log.d("WebRTC", "Local ICE candidate stream 2");

            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                Log.d("WebRTC", "Local ICE candidate data");

            }

            @Override
            public void onRenegotiationNeeded() {
                Log.d("WebRTC", "Local ICE candidate negotiate");

            }
        });

        // Start RTC connection
        peerConnection.createOffer(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new SdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) {}

                    @Override
                    public void onSetSuccess() {}

                    @Override
                    public void onCreateFailure(String s) {
                        // Handle create failure
                    }

                    @Override
                    public void onSetFailure(String s) {
                        // Handle set failure
                    }
                }, sessionDescription);
            }

            @Override
            public void onSetSuccess() {

            }

            @Override
            public void onCreateFailure(String s) {
                // Handle create failure
            }

            @Override
            public void onSetFailure(String s) {
                // Handle set failure
            }
        }, new MediaConstraints());
    }

    public void handleOffer(SessionDescription offer) {
        // Set remote description (offer)
        peerConnection.setRemoteDescription(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {}

            @Override
            public void onSetSuccess() {
                // Handle offer set success
            }

            @Override
            public void onCreateFailure(String s) {
                // Handle create failure
            }

            @Override
            public void onSetFailure(String s) {
                // Handle set failure
            }
        }, offer);
    }
}

