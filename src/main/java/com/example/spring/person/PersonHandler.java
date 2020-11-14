package com.example.spring.person;

import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
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

    public Mono<ServerResponse> remove(ServerRequest request) {
        return Mono.just(request.pathVariable("id"))
            .filter(ObjectId::isValid)
            .flatMapMany(repository::remove)
            .flatMap(count ->
                Flux.just(count)
                    .filter(it -> it > 0)
                    .flatMap(it ->
                        ServerResponse
                            .status(HttpStatus.NO_CONTENT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.empty())
                    )
                    .switchIfEmpty(
                        ServerResponse
                            .status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.empty())
                    )
                    .single()
            )
            .switchIfEmpty(
                ServerResponse
                    .status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.empty())
            )
            .single();
    }
}
