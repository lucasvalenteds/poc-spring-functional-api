package com.example.spring;

import com.example.spring.person.PersonHandler;
import com.example.spring.person.PersonRepository;
import com.example.spring.person.PersonRouter;
import com.mongodb.ConnectionString;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.mongodb.core.ReactiveFluentMongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.netty.http.server.HttpServer;

public final class AppConfiguration {

    private AppConfiguration() {
    }

    public static ApplicationContext createApplicationContext() {
        GenericApplicationContext context = new GenericApplicationContext();

        context.registerBean(ReactiveFluentMongoOperations.class, () ->
            new ReactiveMongoTemplate(new SimpleReactiveMongoDatabaseFactory(new ConnectionString(
                "mongodb://localhost:27017/person"
            )))
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
            .port(8080)
            .handle(new ReactorHttpHandlerAdapter(WebHttpHandlerBuilder.applicationContext(context).build()));
    }
}
