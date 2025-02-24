# Backend
This is a Spring Boot application designed to be used as a backend API for a web application. Spring Boot simplifies the setup and development of Java-based applications by providing built-in features like embedded servers, dependency injection, and production-ready metrics.

## Features

- RESTful API with support for various HTTP methods (GET, POST, PUT, DELETE, PATCH).
- JPA for database interaction with PostgreSQL.
- Built-in security using Spring Security (for authentication).
- Dependency Injection via Spring Framework.

## Setup

### Prerequisites

- JDK 8.
- Maven for dependency management.

### Build
Build the project using Maven:

Maven: `mvn clean install`

### Testing

To test the backend in a Linux environment, use the following command in the `/backend` directory.

```bash
mvn test -Dspring.profiles.active=test
```

To test the backend in a Windows environment, use the following command in the `/backend` directory.

```bash
mvn --% test -Dspring.profiles.active=test
```

To test the frontend, use the following command in the `/frontend` directory.
```bash
npm run test
```