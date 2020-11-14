package com.example.spring.person;

import org.springframework.data.mongodb.core.ReactiveFluentMongoOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public final class PersonRepository {

    private final ReactiveFluentMongoOperations mongo;

    public PersonRepository(ReactiveFluentMongoOperations mongo) {
        this.mongo = mongo;
    }

    public Mono<Person> persist(Person person) {
        return mongo.insert(Person.class).one(person);
    }

    public Flux<Person> findAll() {
        return mongo.query(Person.class).all();
    }
}
