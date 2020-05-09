package com.pelaghisoftware.server.auth.operations;

import akka.actor.ActorRef;
import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCode;
import akka.http.javadsl.model.StatusCodes;
import com.pelaghisoftware.server.auth.JWTObject;
import com.pelaghisoftware.server.response.messages.ErrorMessage;
import com.typesafe.config.ConfigFactory;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

import static akka.pattern.Patterns.ask;

/**
 * Class containing operations used for authorization
 */
public class AuthOperations
{
    //Secret Key used for encoding/decoding a JWT
    private static String secretKey = ConfigFactory.load().getString("api-secret");

    /**
     * Creates a JWT
     * @param id The id for the JWT
     * @param issuer The name of the issuer of the JWT
     * @param subject The subject of the JWT
     * @param ttlMillis Expiration time in Millis
     * @return String. A JWT with the input parameters added.
     */
    public static String createJWT(String id,
                                   String issuer,
                                   String subject,
                                   long ttlMillis)
    {
        //Gets the current Date and Time
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        //Creates the secret key
        SecretKey key = getSecretKey();

        //Builds the JWT
        JwtBuilder builder = Jwts.builder().setId(id)
            .setIssuedAt(now)
            .setSubject(subject)
            .setIssuer(issuer)
            .signWith(key);

        //If there is an expiration, add it to the JWT
        if(ttlMillis > 0)
        {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }

        //Create the JWT String and return it.
        return builder.compact();
    }

    /**
     * Decodes a JWT
     * @param jwt A JWT to try decoding
     * @return Claims from the JWT
     * @throws UnsupportedJwtException
     * @throws MalformedJwtException
     * @throws SignatureException
     * @throws ExpiredJwtException
     * @throws IllegalArgumentException
     */
    public static Claims decodeJWT(String jwt) throws UnsupportedJwtException,
                                                      MalformedJwtException,
                                                      SignatureException,
                                                      ExpiredJwtException,
                                                      IllegalArgumentException
    {
        //Create the builder
        JwtParserBuilder parserBuilder = Jwts.parserBuilder();

        //Parse the JWT. Creates an exception if unsuccessful.
        Claims claims = parserBuilder.setSigningKey(getSecretKey())
            .build()
            .parseClaimsJws(jwt).getBody();

        return claims;
    }

    /**
     * Function to get the Authorization Header from an HttpRequest
     */
    public static final Function<HttpHeader, Optional<JWTObject>> getAuthorizationHeader = header ->
    {
        //Checks if the authorization header is present
        if(header.is("authorization"))
        {
            //Checks to make sure the value is a Bearer token
            if(header.value().contains("Bearer") &&
               header.value().split(" ")[0].equals("Bearer"))
            {
                //Get the JWT from the value
                String jwt = header.value().split(" ")[1];

                return Optional.of(new JWTObject(jwt));
            }
        }
        return Optional.empty();
    };

    /**
     * Checks whether a request is authenticated and then returns a HttpResponse
     * @param authResolver Actor to perform auth operations
     * @param responseResolver Actor to create HttpResponses
     * @param jwt JWT to check
     * @param duration Time duration of async operations before they fail
     * @param response A Supplier that provides the follow on operations after
     *                 an auth check is successful
     * @return CompletionStage a completable future that resolves to an HttpResponse
     */
    public static final CompletionStage<HttpResponse> authCheck(ActorRef authResolver,
                                                                ActorRef responseResolver,
                                                                Optional<JWTObject> jwt,
                                                                Duration duration,
                                                                Supplier<CompletionStage<HttpResponse>> response)
    {
        Boolean authCheck = false;
        //Check for if authentication should happen
        if(authResolver != null)
        {
            authCheck = ask(authResolver, jwt, duration)
                    .thenApply(Boolean.class::cast)
                    .toCompletableFuture()
                    .join();
        }
        //Runs if no auth should be done
        else
        {
            authCheck = true;
        }

        CompletionStage<HttpResponse> result;

        //Run the follow on operations if auth is good to go
        if(authCheck)
        {
            result = response.get();
        }
        //Send Unauthorized message to the user if auth failed
        else
        {
            result = ask(responseResolver, new AuthOperations.Unauthorized(), duration)
                .thenApply(HttpResponse.class::cast);
        }

        return result;
    }

    /**
     * Message that contains a JWT
     */
    public static class JwtMessage
    {
        public JWTObject jwt;

        public JwtMessage(){}

        public JwtMessage(String jwt)
        {
            this.jwt = new JWTObject(jwt);
        }
    }

    /**
     * Message for an unauthorized access
     */
    public static class Unauthorized
    {
        public ErrorMessage message = ErrorMessage.unauthorized();
        public StatusCode code = StatusCodes.UNAUTHORIZED;
    }

    /**
     * Creates a SecretKey
     * @return SecretKey. The api-secret for encoding/decoding a JWT
     */
    private static SecretKey getSecretKey()
    {
        byte[] secret = Base64.getDecoder().decode(secretKey);
        SecretKey key = Keys.hmacShaKeyFor(secret);

        return key;
    }

}
