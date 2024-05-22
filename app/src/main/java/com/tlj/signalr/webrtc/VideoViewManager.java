package com.tlj.signalr.webrtc;

import android.content.Context;

import org.webrtc.SurfaceViewRenderer;
import org.webrtc.EglBase;

public class VideoViewManager {
    private SurfaceViewRenderer remoteVideoView;
    private EglBase eglBase;

    public VideoViewManager(SurfaceViewRenderer remoteVideoView, EglBase eglBase) {
        this.remoteVideoView = remoteVideoView;
        this.eglBase = eglBase;

        remoteVideoView.init(eglBase.getEglBaseContext(), null);
        remoteVideoView.setZOrderMediaOverlay(true);
    }

    public SurfaceViewRenderer getRemoteVideoView() {
        return remoteVideoView;
    }

    public void release() {
        if (remoteVideoView != null) {
            remoteVideoView.release();
        }
    }
}



