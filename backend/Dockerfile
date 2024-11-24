# Stage 1: Build the Spring Boot application
FROM maven:3.8.5-openjdk-17 AS build

# Set the working directory
WORKDIR /app

# Copy the Maven project files
COPY ./pom.xml ./
COPY ./src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Create a minimal image for running the application
FROM openjdk:17-jdk-slim

# Set the UID and GID
ENV USER_ID=3000
ENV GROUP_ID=3000

# Set the working directory
WORKDIR /app

# Hardcode the IP address of plesk1.dyjix.eu in /etc/hosts
RUN echo "185.171.202.140 plesk1.dyjix.eu" >> /etc/hosts  

# Copy the built JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port (default for Spring Boot is 8080)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
