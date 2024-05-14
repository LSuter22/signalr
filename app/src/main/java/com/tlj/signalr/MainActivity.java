package com.tlj.signalr;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;

import java.util.UUID;

import io.reactivex.rxjava3.core.Single;

public class MainActivity extends AppCompatActivity {

    String url = "https://video-chat-api.oski.site/call-hub";
    String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0eXAiOiJhdXRoIiwic3ViIjoiMDc2M2VhMmItZjVjNi00NTBiLTlhNGQtZTE0MTcxMTUyMzZlIiwibmFtZSI6InJlc2lkZW50MiIsImVtYWlsIjoibC5zdXRlckB0bGpncm91cC5jb20iLCJpYXQiOjE3MTU2OTQyOTMsImh0dHA6Ly9zY2hlbWFzLm1pY3Jvc29mdC5jb20vd3MvMjAwOC8wNi9pZGVudGl0eS9jbGFpbXMvcm9sZSI6IlJlc2lkZW50IiwiZXhwIjoxNzE1Njk1NDkzLCJpc3MiOiJodHRwczovL3ZpZGVvLWNoYXQtYXBpLm9za2kuc2l0ZTo0NDMiLCJhdWQiOiJodHRwczovL3ZpZGVvLWNoYXQub3NraS5zaXRlOjQ0MyJ9.m2vvSY76TknQip6FnhuSaCePXZvdoIA-ZxK_Q5rflT8";
    HubConnection hubConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hubConnection = HubConnectionBuilder.create(url)
                .withAccessTokenProvider(Single.defer(() -> Single.just(token)))
                .build();

        hubConnection.on("ReceiveCall", (callId, offer) -> {
            Log.i("SignalR", "Incoming Call");
        }, String.class, String.class);

        hubConnection.on("CallMemberRemoved", () -> {
            // Handle call member removal
        });

        hubConnection.on("ReceiveCandidate", (candidate) -> {
            // Handle received candidate
        }, String.class);

        hubConnection.start().blockingAwait();

        listenCalls();
    }

    public void listenCalls() {
        hubConnection.send("ListenCalls");
    }

    public void joinCall(UUID callId, String answer) {
        hubConnection.send("JoinCall", callId, answer);
    }

    public void leaveCall(UUID callId) {
        hubConnection.send("LeaveCall", callId);
    }

    public void sendCandidate(UUID callId, String candidate) {
        hubConnection.send("SendCandidate", callId, candidate);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hubConnection.close();
    }
}