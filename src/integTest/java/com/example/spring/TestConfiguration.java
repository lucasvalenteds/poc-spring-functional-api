package com.example.spring;

import com.example.spring.person.PersonRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.netty.DisposableServer;

import java.util.Map;

@Testcontainers
public abstract class TestConfiguration {

    @Container
    private static final GenericContainer container = new GenericContainer(DockerImageName.parse("mongo"))
        .withExposedPorts(27017)
        .waitingFor(Wait.forLogMessage("(?i).*waiting for connections.*", 1));

    private static DisposableServer disposableServer;

    protected static PersonRepository repository;
    protected static WebClient client;

    @BeforeAll
    static void beforeAll() {
        String serverPort = "8081";
        String databaseUrl = String.format("mongodb://%s:%d/test", container.getHost(), container.getFirstMappedPort());

        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.getPropertySources()
            .addLast(new MapPropertySource("default", Map.ofEntries(
                Map.entry("server.port", serverPort),
                Map.entry("database.url", databaseUrl)
            )));

        ApplicationContext context = AppConfiguration.createApplicationContext(environment);
        disposableServer = AppConfiguration.createHttpServer(context).bindNow();

        repository = context.getBean(PersonRepository.class);
        client = WebClient.builder()
            .baseUrl(String.format("http://localhost:%d", disposableServer.port()))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @AfterAll
    static void afterAll() {
        disposableServer.dispose();
    }

    TestConfiguration() {
    }
}
