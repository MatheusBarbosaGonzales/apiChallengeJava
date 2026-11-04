package br.com.challenge.challengeApi.config.security;


import br.com.challenge.challengeApi.model.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${tokenSecret}")
    private String secretWord;

    public String generateToken(User user){

        try{
            Algorithm algorithm = Algorithm.HMAC256(secretWord);
            return JWT.create().withIssuer("challengeApi").withSubject(user.getUsername()).withExpiresAt(generateTokenExpirationDate()).sign(algorithm);

        }catch(JWTCreationException e){
            throw new RuntimeException("Coudn't generate token");
        }
    }

    public String validateToken(String token){

        try {

            Algorithm algorithm = Algorithm.HMAC256(secretWord); //codificação do JWT
            return JWT.require(algorithm).withIssuer("challengeApi").build().verify(token).getSubject();

        } catch (JWTVerificationException e) {
            return "";
        }

    }
    private Instant generateTokenExpirationDate(){
        return LocalDateTime.now().plusHours(3).toInstant(ZoneOffset.of("-03:00"));
    }

}
