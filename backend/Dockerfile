# Stage 1: Build the Spring Boot application
FROM maven:3.8.5-openjdk-17 AS spring-builder

# Create working directory
WORKDIR /backend

# Copy the Maven project files
COPY backend/pom.xml .
COPY backend/src ./src

# Package the Spring Boot application
RUN mvn clean package -DskipTests

# Stage 2: Create the final image
FROM openjdk:17-jdk-slim

# Create working directory
WORKDIR /app

# Copy the Spring Boot JAR file from the builder stage
COPY --from=spring-builder /backend/target/*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]