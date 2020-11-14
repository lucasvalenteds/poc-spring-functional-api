package com.example.spring.person;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public final class PersonHandler {

    private final PersonRepository repository;

    public PersonHandler(PersonRepository repository) {
        this.repository = repository;
    }

    public Mono<ServerResponse> persist(ServerRequest request) {
        return ServerResponse
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request.bodyToMono(Person.class).flatMap(repository::persist), Person.class);
    }

    @SuppressWarnings("UNUSED")
    public Mono<ServerResponse> findAll(ServerRequest request) {
        return ServerResponse
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(repository.findAll(), Person.class);
    }
}
