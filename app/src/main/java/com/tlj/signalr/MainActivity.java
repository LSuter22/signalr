package com.tlj.signalr;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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
import com.tlj.signalr.webrtc.WebRTCManager;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.core.Single;

public class MainActivity extends AppCompatActivity {

    HubConnection connection;
    String token = "";
    WebRTCManager webRTCManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webRTCManager = new WebRTCManager(MainActivity.this);

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
            System.out.println("Received Offer: " + offer);
        }, String.class);

        connection.on("ReceiveCandidate", (iceCandidateJSON) -> {
            System.out.println("ICE candidate added successfully");
        }, String.class);

        connection.on("CallIsOver", (callOver) -> {
            System.out.println("Call Over");
        }, Void.class);

        connection.start().blockingAwait();

        if (connection.getConnectionState() == HubConnectionState.CONNECTED) {
            webRTCManager.startRTCConnection();
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
    }
}