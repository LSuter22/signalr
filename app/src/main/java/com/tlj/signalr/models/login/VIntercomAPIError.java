package com.tlj.signalr.models.login;

// MARK: Error Response Models
public class VIntercomAPIError {
    private ErrorDetail[] errors;

    public VIntercomAPIError(ErrorDetail[] errors) {
        this.errors = errors;
    }

    public ErrorDetail[] getErrors() {
        return errors;
    }

    public void setErrors(ErrorDetail[] errors) {
        this.errors = errors;
    }
}
