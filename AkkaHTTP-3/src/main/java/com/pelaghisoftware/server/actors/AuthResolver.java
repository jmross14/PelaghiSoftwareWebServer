package com.pelaghisoftware.server.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import static akka.pattern.Patterns.ask;
import static akka.pattern.Patterns.pipe;

import com.pelaghisoftware.data.actors.operations.DBOperations;
import com.pelaghisoftware.data.entity.User;
import com.pelaghisoftware.server.auth.JWTObject;
import com.pelaghisoftware.server.auth.operations.AuthOperations;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Actor to determine authorization
 */
public class AuthResolver extends AbstractActor
{
    private ActorRef siteUserAccessor;

    /**
     * Create props for a AuthResolver
     * @param siteUserAccessor ActorRef to the SiteUserAccessor Actor
     * @return Props. The props to initialize the actor in the actor system.
     */
    public static Props props(ActorRef siteUserAccessor)
    {
        return Props.create(AuthResolver.class, () -> new AuthResolver(siteUserAccessor));
    }

    /**
     * Constructor
     * @param siteUserAccessor ActorRef to the SiteUserAccessor Actor
     */
    public AuthResolver(ActorRef siteUserAccessor)
    {
        this.siteUserAccessor = siteUserAccessor;
    }

    /**
     * Runs when receiving a message
     * @return
     */
    @Override
    public Receive createReceive()
    {
        return receiveBuilder()
                //Check a login and return a JWT
                .match(User.class, value ->
                {
                   CompletableFuture<AuthOperations.JwtMessage> jwtMessage =
                           ask(siteUserAccessor,
                               new DBOperations.GetEntity(value.getUserName()),
                               Duration.ofSeconds(1))
                           .thenApply(Optional.class::cast)
                           .thenApply(userOption -> checkPassword(value, userOption))
                           .thenApply(Boolean.class::cast)
                           .thenApply(loginValid -> createJWT(loginValid, value.getUserName()))
                           .thenApply(AuthOperations.JwtMessage.class::cast)
                           .toCompletableFuture();

                    pipe(jwtMessage, context().dispatcher()).to(sender());
                })
                //Check a JWT and send whether it is valid back to the sender.
                .match(Optional.class, jwt ->
                {
                    Boolean isValid = true;
                    try
                    {
                        JWTObject toCheck = (JWTObject)jwt.get();
                        Claims claims = AuthOperations.decodeJWT(toCheck.jwt);
                    }
                    catch (UnsupportedJwtException |
                           MalformedJwtException |
                           SignatureException |
                           ExpiredJwtException |
                           IllegalArgumentException |
                           NoSuchElementException e)
                    {
                        isValid = false;
                    }

                    sender().tell(isValid, self());
                })
                .build();
    }

    /**
     * Check an incoming user's password to the database
     * @param user incoming user
     * @param dbUser a known user in the Database
     * @return Boolean. True if valid. Else False.
     */
    private Boolean checkPassword(User user, Optional<User> dbUser)
    {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        Boolean result = false;

        if(dbUser.isPresent())
        {
            result = encoder.matches(user.getEncryptedPassword(),
                                     dbUser.get().getEncryptedPassword());
        }

        return result;
    }

    /**
     * Create a JWT
     * @param loginValid Boolean. login is valid is true
     * @param username String. Username of the user to creat a JWT for
     * @return Message with either a JWT, if valid, or empty if invalid
     */
    private AuthOperations.JwtMessage createJWT(Boolean loginValid,
                                                String username)
    {
        AuthOperations.JwtMessage result;

        //Only build the JWT if the user's login is valid.
        if(loginValid)
        {
            String jwt = AuthOperations.createJWT(UUID.randomUUID().toString(),
                                                  "pelaghisoftware.com",
                                                  username, 86400000);

            result = new AuthOperations.JwtMessage(jwt);
        }
        //Returns an object with no JWT
        else
        {
            result = new AuthOperations.JwtMessage();
        }

        return result;
    }
}
