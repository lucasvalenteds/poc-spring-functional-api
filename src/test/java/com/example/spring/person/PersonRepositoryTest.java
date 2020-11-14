package com.example.spring.person;

import com.example.spring.testing.IntegrationTestConfiguration;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PersonRepositoryTest extends IntegrationTestConfiguration {

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

    @Test
    void testRemoving() {
        Person person = Flux.just(new Person("John Smith")).flatMap(mongo::insert).blockLast();
        assertNotNull(person);
        assertNotNull(person.getId());

        StepVerifier.create(repository.remove(person.getId()))
            .assertNext(it -> assertEquals(1L, it))
            .verifyComplete();

        StepVerifier.create(repository.findAll())
            .expectNextCount(0)
            .verifyComplete();
    }

    @Test
    void testRemovingUnknown() {
        Person person = Flux.just(new Person("John Smith")).flatMap(mongo::insert).blockLast();
        assertNotNull(person);
        assertNotNull(person.getId());

        ObjectId id = ObjectId.get();

        StepVerifier.create(repository.remove(id.toHexString()))
            .assertNext(it -> assertEquals(0L, it))
            .verifyComplete();

        StepVerifier.create(repository.findAll())
            .assertNext(it -> {
                assertEquals(person.getId(), it.getId());
                assertEquals(person.getName(), it.getName());
            })
            .verifyComplete();
    }
}
