package com.example.spring.testing;

import com.mongodb.ConnectionString;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class IntegrationTestConfiguration {

    @Container
    private static final GenericContainer container = new GenericContainer(DockerImageName.parse("mongo"))
        .withExposedPorts(27017)
        .waitingFor(Wait.forLogMessage("(?i).*waiting for connections.*", 1));

    protected final ReactiveMongoTemplate mongo = new ReactiveMongoTemplate(
        new SimpleReactiveMongoDatabaseFactory(
            new ConnectionString(
                String.format("mongodb://%s:%d/test", container.getHost(), container.getFirstMappedPort())
            )
        )
    );

    public IntegrationTestConfiguration() {
    }
}
