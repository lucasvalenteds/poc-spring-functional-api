# POC: Spring Declarative

It demonstrates how to implement a REST API using Spring WebFlux with unit tests, integration tests and end-to-end tests without Spring annotations.

We want a fully reactive application that provides data via HTTP calls, persists data on a MongoDB and allows different testing approaches without using any annotations from Spring Framework or auto-configuration.

Every configuration should be obtained from environment variables or a property file located in the classpath.

We should be able to persist person's name, find all names persisted and remove one name by it's ID.

## How to run

| Description | Command |
| :-- | :-- |
| Provision database | `make provision` |
| Destroy database | `make destroy` |
| Run tests | `./gradlew test` |
| Run application | `./gradlew run` |

