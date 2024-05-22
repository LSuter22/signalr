package com.tlj.signalr.webrtc;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpSender;
import org.webrtc.RtpTransceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoTrack;

import java.util.Arrays;
import java.util.Collections;

public class WebRTCManager {
    private PeerConnectionFactory factory;
    private PeerConnection peerConnection;
    private AudioTrack localAudioTrack;
    private VideoViewManager videoViewManager;
    private EglBase rootEglBase;
    private WebRTCEventListener eventListener;

    public WebRTCManager(Context context, VideoViewManager videoViewManager, WebRTCEventListener eventListener, EglBase rootEglBase) {
        this.videoViewManager = videoViewManager;
        this.eventListener = eventListener;
        this.rootEglBase = rootEglBase;

        // Initialize PeerConnectionFactory
        PeerConnectionFactory.InitializationOptions initializationOptions = PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();

        VideoEncoderFactory encoderFactory = new DefaultVideoEncoderFactory(
                rootEglBase.getEglBaseContext(), true, true);
        VideoDecoderFactory decoderFactory = new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());

        factory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoDecoderFactory(decoderFactory)
                .setVideoEncoderFactory(encoderFactory)
                .createPeerConnectionFactory();

        // Create AudioSource
        MediaConstraints audioConstraints = new MediaConstraints();
        AudioSource audioSource = factory.createAudioSource(audioConstraints);

        // Create AudioTrack
        localAudioTrack = factory.createAudioTrack("ARDAMSa0", audioSource);
    }

    public void closeConnection() {
        if (peerConnection != null) {
            peerConnection.close();
            peerConnection = null;
        }
    }

    public void startRTCConnection() {
        // Create configuration
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(
                Arrays.asList(
                        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
                        PeerConnection.IceServer.builder("turn:eu-0.turn.peerjs.com:3478")
                                .setUsername("peerjs")
                                .setPassword("peerjsp")
                                .createIceServer(),
                        PeerConnection.IceServer.builder("turn:us-0.turn.peerjs.com:3478")
                                .setUsername("peerjs")
                                .setPassword("peerjsp")
                                .createIceServer()
                )
        );

        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;

        // Create PeerConnection
        peerConnection = factory.createPeerConnection(rtcConfig, new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                Log.d("WebRTC", "Signaling state: " + signalingState);
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Log.d("WebRTC", "ICE connection state: " + iceConnectionState);
                switch (iceConnectionState){
                    case CONNECTED:
                        Log.i("IceState", "Connected");
                        break;
                    case CHECKING:
                        Log.i("IceState", "Checking");
                        break;
                    case CLOSED:
                        Log.i("IceState", "Closed");
                        break;
                    case COMPLETED:
                        Log.i("IceState", "Completed");
                        break;
                    case DISCONNECTED:
                        Log.i("IceState", "Disconnected");
                        break;
                    case NEW:
                        Log.i("IceState", "New");
                        break;
                    case FAILED:
                        Log.i("IceState", "Failed");
                        break;
                }
            }

            @Override
            public void onIceConnectionReceivingChange(boolean receiving) {
                Log.d("WebRTC", "ICE connection receiving change: " + receiving);
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                switch (iceGatheringState){
                    case NEW:
                        Log.i("IceGatheringState", "New");
                        break;
                    case GATHERING:
                        Log.i("IceGatheringState", "Gathering");
                        break;
                    case COMPLETE:
                        Log.i("IceGatheringState", "Complete");
                        break;
                }
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                Log.d("WebRTC", "ICE candidate: " + iceCandidate);
                try {
                    JSONObject json = new JSONObject();
                    json.put("sdpMid", iceCandidate.sdpMid);
                    json.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
                    json.put("candidate", iceCandidate.sdp);
                    String iceCandidateJson = json.toString();

                    // Call the method in MainActivity
                    if (eventListener != null) {
                        eventListener.onIceCandidateReceived(iceCandidateJson);
                    }
                } catch (Exception e) {
                    Log.e("WebRTC", "Failed to convert ICE candidate to JSON: " + e.getMessage());
                }
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                Log.d("WebRTC", "ICE candidates removed");
            }

            @Override
            public void onAddStream(org.webrtc.MediaStream mediaStream) {
                Log.d("WebRTC", "Stream added: " + mediaStream);

                // Play the incoming audio track
                if (mediaStream.audioTracks.size() > 0) {
                    AudioTrack remoteAudioTrack = mediaStream.audioTracks.get(0);
                    remoteAudioTrack.setEnabled(true);
                    remoteAudioTrack.setVolume(1.0f); // Ensure volume is set
                    Log.d("WebRTC", "Remote audio track enabled: " + remoteAudioTrack.id());
                }

                // Play the incoming video track
                if (mediaStream.videoTracks.size() > 0) {
                    VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
                    remoteVideoTrack.addSink(videoViewManager.getRemoteVideoView());
                    Log.d("WebRTC", "Remote video track added: " + remoteVideoTrack.id());
                }
            }

            @Override
            public void onRemoveStream(org.webrtc.MediaStream mediaStream) {
                Log.d("WebRTC", "Stream removed: " + mediaStream);
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                Log.d("WebRTC", "Data channel: " + dataChannel);
            }

            @Override
            public void onRenegotiationNeeded() {
                Log.d("WebRTC", "Renegotiation needed");
            }

            @Override
            public void onTrack(RtpTransceiver transceiver) {
                if (transceiver.getReceiver().track() instanceof VideoTrack) {
                    VideoTrack remoteVideoTrack = (VideoTrack) transceiver.getReceiver().track();
                    remoteVideoTrack.addSink(videoViewManager.getRemoteVideoView());
                    Log.d("WebRTC", "Remote video track added: " + remoteVideoTrack.id());
                } else if (transceiver.getReceiver().track() instanceof AudioTrack) {
                    AudioTrack remoteAudioTrack = (AudioTrack) transceiver.getReceiver().track();
                    remoteAudioTrack.setEnabled(true);
                    remoteAudioTrack.setVolume(1.0f); // Ensure volume is set
                    Log.d("WebRTC", "Remote audio track added: " + remoteAudioTrack.id());
                }
            }
        });

        // Add local audio track to PeerConnection using addTrack
        RtpSender audioSender = peerConnection.addTrack(localAudioTrack, Collections.singletonList("ARDAMS"));
        if (audioSender == null) {
            Log.e("WebRTC", "Failed to add audio track to PeerConnection.");
        } else {
            Log.d("WebRTC", "Local audio track added to PeerConnection: " + localAudioTrack.id());
        }

        // Create offer with constraints
        MediaConstraints offerConstraints = new MediaConstraints();
        offerConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        offerConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

        peerConnection.createOffer(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new SdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) {}

                    @Override
                    public void onSetSuccess() {
                        Log.d("WebRTC", "Local description set successfully.");
                    }

                    @Override
                    public void onCreateFailure(String s) {
                        Log.e("WebRTC", "Failed to create local description: " + s);
                    }

                    @Override
                    public void onSetFailure(String s) {
                        Log.e("WebRTC", "Failed to set local description: " + s);
                    }
                }, sessionDescription);
            }

            @Override
            public void onSetSuccess() {}

            @Override
            public void onCreateFailure(String s) {
                Log.e("WebRTC", "Failed to create offer: " + s);
            }

            @Override
            public void onSetFailure(String s) {
                Log.e("WebRTC", "Failed to set offer: " + s);
            }
        }, offerConstraints);
    }

    public void createAnswer(String offer, final AnswerCallback callback) {
        Log.d("WebRTC", "createAnswer start: " + offer);

        try {
            JSONObject json = new JSONObject(offer);
            String sdp = json.getString("sdp");
            String typeString = json.getString("type");
            SessionDescription.Type type = SessionDescription.Type.fromCanonicalForm(typeString);

            final SessionDescription remoteDescription = new SessionDescription(type, sdp);

            if (peerConnection.signalingState() == PeerConnection.SignalingState.HAVE_LOCAL_OFFER) {
                peerConnection.setLocalDescription(new SdpObserver() {
                    @Override
                    public void onSetSuccess() {
                        setRemoteDescriptionAndCreateAnswer(remoteDescription, callback);
                    }

                    @Override
                    public void onSetFailure(String s) {
                        Log.e("WebRTC", "Failed to rollback local description: " + s);
                        callback.onAnswerCreated(null);
                    }

                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) {}

                    @Override
                    public void onCreateFailure(String s) {}
                }, new SessionDescription(SessionDescription.Type.ROLLBACK, ""));
            } else {
                setRemoteDescriptionAndCreateAnswer(remoteDescription, callback);
            }
        } catch (Exception e) {
            Log.e("WebRTC", "Failed to parse offer JSON or extract SDP/type: " + e.getMessage());
            callback.onAnswerCreated(null);
        }
    }

    private void setRemoteDescriptionAndCreateAnswer(final SessionDescription remoteDescription, final AnswerCallback callback) {
        peerConnection.setRemoteDescription(new SdpObserver() {
            @Override
            public void onSetSuccess() {
                MediaConstraints answerConstraints = new MediaConstraints();
                answerConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
                answerConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

                peerConnection.createAnswer(new SdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) {
                        peerConnection.setLocalDescription(new SdpObserver() {
                            @Override
                            public void onSetSuccess() {
                                Log.d("WebRTC", "createAnswer end: " + remoteDescription.description);
                                callback.onAnswerCreated(sessionDescription.description);
                            }

                            @Override
                            public void onSetFailure(String s) {
                                Log.e("WebRTC", "Failed to set local description: " + s);
                                callback.onAnswerCreated(null);
                            }

                            @Override
                            public void onCreateSuccess(SessionDescription sessionDescription) {}

                            @Override
                            public void onCreateFailure(String s) {}
                        }, sessionDescription);
                    }

                    @Override
                    public void onCreateFailure(String s) {
                        Log.e("WebRTC", "Failed to create answer: " + s);
                        callback.onAnswerCreated(null);
                    }

                    @Override
                    public void onSetSuccess() {}

                    @Override
                    public void onSetFailure(String s) {}
                }, answerConstraints);
            }

            @Override
            public void onSetFailure(String s) {
                Log.e("WebRTC", "Failed to set remote description: " + s);
                callback.onAnswerCreated(null);
            }

            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {}

            @Override
            public void onCreateFailure(String s) {}
        }, remoteDescription);
    }

    public void addCandidate(IceCandidate iceCandidate) {
        try {
            peerConnection.addIceCandidate(iceCandidate);
        } catch (Exception e) {
            Log.e("WebRTC", "Failed to Add ICE candidate JSON: " + e.getMessage());
        }
    }

    public interface AnswerCallback {
        void onAnswerCreated(String answerSdp);
    }

    // WebRTCEventListener.java
    public interface WebRTCEventListener {
        void onIceCandidateReceived(String iceCandidateJson);
    }
}




