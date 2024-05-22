package com.tlj.signalr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.tlj.signalr.helpers.Virtual_Int_API;
import com.tlj.signalr.models.IncomingUnitCallInfo;
import com.tlj.signalr.models.IncomingUnitMessageInfo;
import com.tlj.signalr.models.MissedCall;
import com.tlj.signalr.models.login.LoginResponse;
import com.tlj.signalr.models.login.TokenInfo;
import com.tlj.signalr.models.login.VIntercomAPIError;
import com.tlj.signalr.models.login.ErrorDetail;
import com.tlj.signalr.webrtc.VideoViewManager;
import com.tlj.signalr.webrtc.WebRTCManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.SurfaceViewRenderer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.core.Single;

public class MainActivity extends AppCompatActivity implements WebRTCManager.WebRTCEventListener {

    private static final String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.ACCESS_WIFI_STATE,
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.INTERNET
    };

    HubConnection connection;
    String token = "";
    WebRTCManager webRTCManager;
    VideoViewManager videoViewManager;

    String callid = "";
    EglBase rootEglBase;
    SurfaceViewRenderer remoteVideoView;


    private static final int PERMISSION_REQUEST_CODE = 1;

    public void checkPermissions(Context context, Activity activity){
        int BLEPerm = ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
        if (BLEPerm != PackageManager.PERMISSION_GRANTED) {
            //Prompting User If Permissions Are Not Granted
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    1
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize EglBase
        rootEglBase = EglBase.create();

        // Initialize SurfaceViewRenderer
        remoteVideoView = findViewById(R.id.local_video_view);
        remoteVideoView.setZOrderMediaOverlay(true);

        // Initialize VideoViewManager
        videoViewManager = new VideoViewManager(remoteVideoView, rootEglBase);

        // Initialize WebRTCManager
        webRTCManager = new WebRTCManager(this, videoViewManager, this,rootEglBase);


        login();
    }

    private void login(){
        Executor executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            Virtual_Int_API IntercomAPI = new Virtual_Int_API();

            IntercomAPI.loginUser("l.suter@tljgroup.com", "Qwerty-123", true,
                    loginResponse -> {
                        runOnUiThread(() -> {
                            Log.i("Intercom Login","Login successful");
                            Log.i("Intercom Login","Auth Token: " + loginResponse.getAuthToken().getToken());
                            Log.i("Intercom Login","Refresh Token: " + loginResponse.getRefreshToken().getToken());
                            token = loginResponse.getAuthToken().getToken();
                            setupHub();
                        });
                    },
                    error -> {
                        runOnUiThread(() -> Log.i("Intercom Login","Login failed: " + error));
                    });
        });
    }
    private void setupHub(){
        connection = HubConnectionBuilder.create("https://video-chat-api.oski.site/call-hub")
                .withAccessTokenProvider(Single.defer(() -> Single.just(token)))
                .build();

        connection.on("IncomingCall", (incomingCall) -> {
            System.out.println("Incoming Call, Call ID" + incomingCall.getCallId());
            callid = incomingCall.getCallId();
            webRTCManager.startRTCConnection();
            connection.invoke("AnswerCall", incomingCall.getCallId()).blockingAwait();
        }, IncomingUnitCallInfo.class);

        connection.on("IncomingMessage", (incomingMessage) -> {
            System.out.println("Incoming Message, Call ID" + incomingMessage.getCallId());
        }, IncomingUnitMessageInfo.class);

        connection.on("MissedCall", (missedCall) -> {
            System.out.println("Missed Call, When" + missedCall.getWhen());
        }, MissedCall.class);

        connection.on("CallMemberRemoved", (memberRemoved) -> {
            System.out.println("Call Member Removed");
        }, Void.class);

        connection.on("ReceiveOffer", (offer) -> {
            webRTCManager.createAnswer(offer, answerSdp -> {
                if (answerSdp != null) {
                    connection.invoke("SendAnswer", callid, answerSdp);
                } else {
                    Log.i("WebRTC Answer", "Error");
                }
            });
        }, String.class);

        connection.on("ReceiveCandidate", (iceCandidateJSON) -> {
            System.out.println("ICE candidate Receive");

            try {
                JSONObject json = new JSONObject(iceCandidateJSON);
                String sdpMid = json.getString("sdpMid");
                int sdpMLineIndex = json.getInt("sdpMLineIndex");
                String candidate = json.getString("candidate");
                IceCandidate iceCandidate = new IceCandidate(sdpMid, sdpMLineIndex, candidate);
                webRTCManager.addCandidate(iceCandidate);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }, String.class);

        connection.on("CallIsOver", () -> {
            System.out.println("Call Over");
            webRTCManager.closeConnection();
        });

        connection.start().blockingAwait();

        if (connection.getConnectionState() == HubConnectionState.CONNECTED) {
            listenCalls();
        }
    }

    private void listenCalls() {
        connection.send("ListenCalls");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connection.close();
        webRTCManager.closeConnection();
        videoViewManager.release();

    }

    @Override
    public void onIceCandidateReceived(String iceCandidateJson) {
        Log.i("SignalR","Send Ice Candid: "+ iceCandidateJson);
        Log.i("SignalR","Send Ice Candid Call ID: "+ callid);
       connection.invoke("SendCandidate",callid,iceCandidateJson);
    }
}