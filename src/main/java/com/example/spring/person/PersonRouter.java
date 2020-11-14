package com.example.spring.person;

import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

public final class PersonRouter {

    private final PersonHandler handler;

    public PersonRouter(PersonHandler handler) {
        this.handler = handler;
    }

    public RouterFunction<ServerResponse> create() {
        return RouterFunctions.route()
            .nest(RequestPredicates.path("/person"), () ->
                RouterFunctions.route()
                    .GET(handler::findAll)
                    .POST(handler::persist)
                    .build()
            )
            .build();
    }
}
