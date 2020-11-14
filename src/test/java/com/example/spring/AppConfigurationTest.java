package com.example.spring;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

class AppConfigurationTest {

    @Test
    void testReadingPropertiesFile() {
        ConfigurableEnvironment environment = new AppConfiguration.PropertyBasedEnvironment("example.properties");

        assertEquals("exists", environment.getRequiredProperty("some.property"));
    }

    @Test
    void testReadingApplicationPropertiesFile() {
        ConfigurableEnvironment environment = AppConfiguration.environment();

        assertTrue(environment.containsProperty("server.port"));
        assertTrue(environment.containsProperty("database.url"));
    }
}
