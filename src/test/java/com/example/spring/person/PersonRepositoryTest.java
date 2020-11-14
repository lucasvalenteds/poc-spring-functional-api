package com.example.spring.person;

import com.mongodb.ConnectionString;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PersonRepositoryTest {

    private final ReactiveMongoTemplate mongo = new ReactiveMongoTemplate(
        new SimpleReactiveMongoDatabaseFactory(
            new ConnectionString("mongodb://localhost:27017/person2")
        )
    );

    private final PersonRepository repository = new PersonRepository(mongo);

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

        Flux<String> names = repository
            .findAll()
            .map(Person::getName);

        StepVerifier.create(names)
            .assertNext((name) -> assertEquals("John Smith", name))
            .assertNext((name) -> assertEquals("Mary Jane", name))
            .verifyComplete();
    }
}
