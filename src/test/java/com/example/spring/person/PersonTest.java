package com.example.spring.person;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersonTest {

    @Test
    void testPersonHasName() {
        String name = "John Smith";

        Person person = new Person(name);

        assertEquals(name, person.getName());
    }
}
