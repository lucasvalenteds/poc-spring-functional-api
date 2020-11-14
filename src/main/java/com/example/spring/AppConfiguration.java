package com.example.spring;

import com.example.spring.person.PersonHandler;
import com.example.spring.person.PersonRepository;
import com.example.spring.person.PersonRouter;
import com.mongodb.ConnectionString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.data.mongodb.core.ReactiveFluentMongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.netty.http.server.HttpServer;

import java.io.IOException;

public final class AppConfiguration {

    private static final Logger LOGGER = LogManager.getLogger(AppConfiguration.class);

    private AppConfiguration() {
    }

    public static class PropertyBasedEnvironment extends StandardEnvironment {

        public PropertyBasedEnvironment(String filename) {
            try {
                getPropertySources().addLast(new ResourcePropertySource(filename));
            } catch (IOException exception) {
                LOGGER.error("Could not read the file {}.", filename);
                System.exit(1);
            }
        }
    }

    public static ConfigurableEnvironment environment() {
        return new PropertyBasedEnvironment("classpath:/application.properties");
    }

    public static ApplicationContext createApplicationContext(ConfigurableEnvironment environment) {
        GenericApplicationContext context = new GenericApplicationContext();
        context.setEnvironment(environment);

        context.registerBean(ConnectionString.class, () ->
            new ConnectionString(environment.getRequiredProperty("database.url", String.class))
        );
        context.registerBean(ReactiveFluentMongoOperations.class, () ->
            new ReactiveMongoTemplate(new SimpleReactiveMongoDatabaseFactory(context.getBean(ConnectionString.class)))
        );
        context.registerBean(WebHttpHandlerBuilder.WEB_HANDLER_BEAN_NAME, WebHandler.class, () ->
            RouterFunctions.toWebHandler(context.getBean(PersonRouter.class).create())
        );

        context.registerBean(PersonRepository.class);
        context.registerBean(PersonHandler.class);
        context.registerBean(PersonRouter.class);

        context.refresh();

        return context;
    }

    public static HttpServer createHttpServer(ApplicationContext context) {
        return HttpServer.create()
            .port(context.getEnvironment().getRequiredProperty("server.port", Integer.class))
            .handle(new ReactorHttpHandlerAdapter(WebHttpHandlerBuilder.applicationContext(context).build()));
    }
}
