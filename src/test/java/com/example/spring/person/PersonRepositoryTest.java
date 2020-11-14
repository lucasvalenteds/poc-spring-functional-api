package com.example.spring.person;

import com.mongodb.ConnectionString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
class PersonRepositoryTest {

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

    @BeforeEach
    void beforeEach() {
        mongo.dropCollection(Person.class).block();
    }

    @Test
    void testPersisting() {
        Person person = new Person("John Smith");

        StepVerifier.create(repository.persist(person))
            .assertNext(it -> assertEquals("John Smith", it.getName()))
            .verifyComplete();
    }

    @Test
    void testFindingAll() {
        Flux.just("John Smith", "Mary Jane")
            .map(Person::new)
            .flatMap(mongo::insert)
            .blockLast();

        StepVerifier.create(repository.findAll())
            .assertNext((person) -> {
                assertNotNull(person.getId());
                assertEquals("John Smith", person.getName());
            })
            .assertNext((person) -> {
                assertNotNull(person.getId());
                assertEquals("Mary Jane", person.getName());
            })
            .verifyComplete();
    }
}
