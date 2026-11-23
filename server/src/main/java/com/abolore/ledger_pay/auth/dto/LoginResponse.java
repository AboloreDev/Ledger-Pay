package com.abolore.ledger_pay.auth.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {

    public static LoginResponse bearer(String accessToken, long expiresIn) {
        return new LoginResponse(accessToken, "Bearer", expiresIn);
    }

}
