package com.tlj.signalr.helpers;

import com.google.gson.Gson;
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Virtual_Int_API {
    public void loginUser(String login, String password, boolean remember,
                          LoginResponseListener onSuccess, FailureListener onFailure) {

        String url = "https://video-chat-api.oski.site/api/auth/login";
        Gson gson = new Gson();

        try {
            // Construct JSON request manually
            String requestData = "{\"login\":\"" + login + "\",\"password\":\"" + password + "\",\"remember\":" + remember + "}";

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("accept", "text/plain");
            conn.setDoOutput(true);
            conn.getOutputStream().write(requestData.getBytes());

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                LoginResponse loginResponse = gson.fromJson(new InputStreamReader(conn.getInputStream()), LoginResponse.class);
                onSuccess.onSuccess(loginResponse);
            } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                VIntercomAPIError errorResponse = gson.fromJson(new InputStreamReader(conn.getErrorStream()), VIntercomAPIError.class);
                onFailure.onFailure("Failed to authenticate user: " + errorResponse.getErrors()[0].getMessage());
            } else {
                onFailure.onFailure("Unhandled HTTP response status code: " + responseCode);
            }
            conn.disconnect();
        } catch (IOException e) {
            onFailure.onFailure("Error making request: " + e.getMessage());
        }
    }


    public interface LoginResponseListener {
        void onSuccess(LoginResponse loginResponse);
    }

    public interface FailureListener {
        void onFailure(String errorMessage);
    }

}


