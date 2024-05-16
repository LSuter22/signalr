package com.tlj.signalr.models.login;

// MARK: Login Response Models
public class LoginResponse {
    private TokenInfo authToken;
    private TokenInfo refreshToken;

    public LoginResponse(TokenInfo authToken, TokenInfo refreshToken) {
        this.authToken = authToken;
        this.refreshToken = refreshToken;
    }

    public TokenInfo getAuthToken() {
        return authToken;
    }

    public void setAuthToken(TokenInfo authToken) {
        this.authToken = authToken;
    }

    public TokenInfo getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(TokenInfo refreshToken) {
        this.refreshToken = refreshToken;
    }
}
