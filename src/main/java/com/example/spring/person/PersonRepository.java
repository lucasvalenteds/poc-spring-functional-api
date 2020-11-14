package com.example.spring.person;

import com.mongodb.client.result.DeleteResult;
import org.springframework.data.mongodb.core.ReactiveFluentMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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

    public Mono<Long> remove(String id) {
        return mongo.remove(Person.class)
            .matching(new Query(Criteria.where("id").is(id)))
            .all()
            .map(DeleteResult::getDeletedCount)
            .defaultIfEmpty(0L);
    }
}
