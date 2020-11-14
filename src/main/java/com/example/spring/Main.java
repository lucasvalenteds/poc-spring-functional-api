package com.example.spring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import reactor.netty.http.server.HttpServer;

import java.time.Duration;

public class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        ApplicationContext context = AppConfiguration.createApplicationContext();
        HttpServer httpServer = AppConfiguration.createHttpServer(context);

        httpServer.bindUntilJavaShutdown(
            Duration.ofSeconds(5),
            server -> LOGGER.info("Server running on port {}", server.port())
        );
    }
}
