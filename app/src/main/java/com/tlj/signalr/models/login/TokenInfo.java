package com.tlj.signalr.models.login;

import java.util.Date;

public class TokenInfo {
    private String policy;
    private String token;
    private Date expires;

    public TokenInfo(String policy, String token, Date expires) {
        this.policy = policy;
        this.token = token;
        this.expires = expires;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }
}
