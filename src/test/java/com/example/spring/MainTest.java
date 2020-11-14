package com.example.spring;

import com.example.spring.person.Person;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest extends TestConfiguration {

    @BeforeEach
    void beforeEach() {
        repository.findAll()
            .map(Person::getId)
            .flatMap(repository::remove)
            .blockLast();

        Flux.just("John Smith", "Mary Jane")
            .map(Person::new)
            .flatMap(repository::persist)
            .blockLast();
    }

    @Test
    void testPersisting() {
        Mono<Person> person = client.post().uri("/person")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue("{\"name\":\"John Smith\"}"))
            .exchangeToMono(response -> {
                assertEquals(HttpStatus.OK, response.statusCode());

                Optional<MediaType> contentType = response.headers().contentType();
                assertTrue(contentType.isPresent());
                assertEquals(MediaType.APPLICATION_JSON, contentType.get());

                return response.bodyToMono(Person.class);
            });

        StepVerifier.create(person)
            .assertNext(it -> {
                assertTrue(ObjectId.isValid(it.getId()));
                assertEquals("John Smith", it.getName());
            })
            .verifyComplete();
    }

    @Test
    void testFindingAll() {
        List<Person> list = client.get().uri("/person")
            .exchangeToMono(response -> {
                assertEquals(HttpStatus.OK, response.statusCode());

                Optional<MediaType> contentType = response.headers().contentType();
                assertTrue(contentType.isPresent());
                assertEquals(MediaType.APPLICATION_JSON, contentType.get());

                return response.bodyToMono(new ParameterizedTypeReference<List<Person>>() {
                });
            })
            .defaultIfEmpty(List.of())
            .block();

        assertEquals(2, list.size());
        assertNotNull(list.get(0).getId());
        assertEquals("John Smith", list.get(0).getName());
        assertNotNull(list.get(1).getId());
        assertEquals("Mary Jane", list.get(1).getName());
    }

    @Test
    void testRemoving() {
        Person person = repository.findAll().blockLast();
        assertNotNull(person);
        assertNotNull(person.getId());

        Mono<Void> emptyBody = client.delete().uri("/person/".concat(person.getId()))
            .exchangeToMono(response -> {
                assertEquals(HttpStatus.NO_CONTENT, response.statusCode());

                Optional<MediaType> contentType = response.headers().contentType();
                assertTrue(contentType.isPresent());
                assertEquals(MediaType.APPLICATION_JSON, contentType.get());

                return response.releaseBody();
            });

        StepVerifier.create(emptyBody)
            .expectNextCount(0)
            .verifyComplete();
    }
}
