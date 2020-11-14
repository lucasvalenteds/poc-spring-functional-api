package com.example.spring.person;

import com.mongodb.ConnectionString;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PersonRouterIntegrationTest {

    private final ReactiveMongoTemplate mongo = new ReactiveMongoTemplate(
        new SimpleReactiveMongoDatabaseFactory(
            new ConnectionString("mongodb://localhost:27017/person1")
        )
    );

    private final PersonRepository repository = new PersonRepository(mongo);

    private final PersonHandler handler = new PersonHandler(repository);

    private final PersonRouter router = new PersonRouter(handler);

    private final WebTestClient client = WebTestClient
        .bindToRouterFunction(router.create())
        .build();

    @BeforeEach
    void beforeEach() {
        mongo.dropCollection(Person.class).block();
    }

    @AfterAll
    void afterAll() {
        mongo.dropCollection(Person.class).block();
    }

    @Test
    void testPersisting() {
        Person person = client.post().uri("/person")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue("{\"name\":\"John Smith\"}"))
            .exchange()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectStatus().isOk()
            .expectBody(Person.class)
            .returnResult()
            .getResponseBody();

        assertNotNull(person);
        assertEquals("John Smith", person.getName());
    }

    @Test
    void testFindingAll() {
        Flux.just("John Smith", "Mary Jane")
            .map(Person::new)
            .flatMap(mongo::insert)
            .blockLast();

        client.get().uri("/person").exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBodyList(Person.class)
            .hasSize(2)
            .contains(new Person("John Smith"))
            .contains(new Person("Mary Jane"));
    }
}
