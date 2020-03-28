package com.pelaghisoftware.server;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.pelaghisoftware.entity.User;

import java.util.concurrent.CompletionStage;

public class Main extends AllDirectives
{
    /**
     * Main Method
     * @param args Arguments passed in when starting the program
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        //Create the parent actor system that will be used to process everything.
        ActorSystem system = ActorSystem.create("routes");

        //Server instance
        final Http http = Http.get(system);

        //Used to process the requests into responses
        final ActorMaterializer materializer = ActorMaterializer.create(system);


        //In order to access all directives we need an instance where the routes are defined. I used Main as my class
        //name. Use whatever you name your class.
        Main app = new Main();

        //Maps the routes into the system allowing the HttpRequest to be used and output a HttpRequest back to the User.
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materializer);

        //Binds the server to a port to start accepting requests
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow, ConnectHttp.toHost("localhost", 8099), materializer);

        System.out.println("Server online at http://localhost:8099/\nPress Return to stop...");
        System.in.read(); //let it run until user presses return

        binding
                .thenCompose(ServerBinding::unbind) //trigger unbinding from the port
                .thenAccept(unbound -> system.terminate()); // and shutdown when done
    }

    private Route createRoute()
    {
        return concat(
                path("hello", () ->
                        get(() ->
                            complete(
                                    HttpEntities.create(ContentTypes.TEXT_HTML_UTF8,
                                        "<html>" +
                                            "<body>" +
                                                "<h1>Hello world!</h1>" +
                                            "</body>" +
                                    "</html>")))),
                path("user", () ->
                        get(() ->
                            complete(
                                    StatusCodes.OK,
                                    new User("test", "test"),
                                    Jackson.<User>marshaller()))),
                        post(() ->
                            entity(
                                    Jackson.unmarshaller(User.class), user ->
                                    {
                                        System.out.println("Username: " + user.getUserName() + " Password: " + user.getPassword());
                                        return complete(StatusCodes.OK);
                                    })));
    }
}
