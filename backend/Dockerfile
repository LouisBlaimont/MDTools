# Stage 1: Build the Spring Boot application
FROM maven:3.8.5-openjdk-17 AS build

# Set the working directory
WORKDIR /app

# Copy the Maven project files
COPY ./pom.xml ./
COPY ./src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Run tests
FROM maven:3.8.5-openjdk-17 AS test
WORKDIR /app

# Copy project files
COPY --from=build /app /app

# Run tests
CMD ["mvn", "test"]

# Stage 3: Create a minimal image for running the application
FROM eclipse-temurin:17-jdk-jammy

# Set the UID and GID
ENV USER_ID=3000
ENV GROUP_ID=3000

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

COPY wild.apps.speam.montefiore.uliege.be__cacert.crt /usr/local/share/ca-certificates/wild.apps.speam.montefiore.uliege.be__cacert.crt

RUN update-ca-certificates && \
    keytool -import -noprompt -trustcacerts -alias oc_cert -file /usr/local/share/ca-certificates/wild.apps.speam.montefiore.uliege.be__cacert.crt -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit

# Expose the application port (default for Spring Boot is 8080)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
