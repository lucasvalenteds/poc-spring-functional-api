package com.example.spring.person;

import com.mongodb.client.result.DeleteResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.mongodb.core.ReactiveFluentMongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public final class PersonRepository {

    private static final Logger LOGGER = LogManager.getLogger(PersonRepository.class);

    private final ReactiveFluentMongoOperations mongo;

    public PersonRepository(ReactiveFluentMongoOperations mongo) {
        this.mongo = mongo;
    }

    public Mono<Person> persist(Person person) {
        return mongo.insert(Person.class).one(person)
            .doOnNext(it -> LOGGER.info("Person persisted successfully: {}", it))
            .doOnError(it -> LOGGER.warn("Could not persist a person", it));
    }

    public Flux<Person> findAll() {
        return mongo.query(Person.class).all()
            .doOnComplete(() -> LOGGER.info("All persons queried"))
            .doOnError(it -> LOGGER.warn("Could find all persons", it));
    }

    public Mono<Long> remove(String id) {
        return mongo.remove(Person.class)
            .matching(new Query(Criteria.where("id").is(id)))
            .all()
            .map(DeleteResult::getDeletedCount)
            .defaultIfEmpty(0L)
            .doOnNext(it -> LOGGER.info("Person removed successfully: {}", it))
            .doOnError(it -> LOGGER.warn("Could not remove a person", it));
    }
}
