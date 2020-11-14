package com.example.spring.person;

import com.mongodb.ConnectionString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
class PersonRouterTest {

    @Container
    private static final GenericContainer container = new GenericContainer(DockerImageName.parse("mongo"))
        .withExposedPorts(27017)
        .waitingFor(Wait.forLogMessage("(?i).*waiting for connections.*", 1));

    private final ReactiveMongoTemplate mongo = new ReactiveMongoTemplate(
        new SimpleReactiveMongoDatabaseFactory(
            new ConnectionString(
                String.format("mongodb://%s:%d/test", container.getHost(), container.getFirstMappedPort())
            )
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
        assertNotNull(person.getId());
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
            .value(list -> {
                assertNotNull(list.get(0).getId());
                assertEquals("John Smith", list.get(0).getName());

                assertNotNull(list.get(1).getId());
                assertEquals("Mary Jane", list.get(1).getName());
            });
    }
}