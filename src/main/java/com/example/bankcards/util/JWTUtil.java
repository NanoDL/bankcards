package com.example.bankcards.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;

@Component
public class JWTUtil {
    @Value("${jwt.secret}")
    private String secret;

    private final UserRepository userRepository;

    @Autowired
    public JWTUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateToken(String username){
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));

        Date expitrationDate = Date.from(ZonedDateTime.now().plusMinutes(60).toInstant());

        return JWT.create()
                .withSubject("User details")
                .withClaim("id", user.getId())
                .withClaim("username", user.getUsername())
                .withClaim("role", user.getRole().name())
                .withIssuedAt(new Date())
                .withIssuer("bank-app")
                .withExpiresAt(expitrationDate)
                .sign(Algorithm.HMAC256(secret));
    }


    public HashMap<String, Claim> validateTokenAndRetrieveClaims(String token){
        DecodedJWT jwt = validate(token);
        return new HashMap<>(jwt.getClaims());
    }

    public String validateTokenAndRetrieveUsername(String token) {
        DecodedJWT jwt = validate(token);
        System.out.println(jwt.getClaim("role").asString());
        return jwt.getClaim("username").asString();
    }

    public String validateTokenAndRetrieveRole(String token){
        DecodedJWT jwt = validate(token);
        return jwt.getClaim("role").asString();
    }

    private DecodedJWT validate(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User details")
                .withIssuer("bank-app")
                .build();
        return verifier.verify(token);
    }
}
