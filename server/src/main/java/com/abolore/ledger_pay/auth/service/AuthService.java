package com.abolore.ledger_pay.auth.service;

import java.util.Locale;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.abolore.ledger_pay.auth.dto.LoginRequest;
import com.abolore.ledger_pay.auth.dto.LoginResponse;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JWTService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        loginRequest.email().trim().toLowerCase(Locale.ROOT),
                        loginRequest.password()));

        return jwtService.generateToken(authentication);

    }
}
