
# MDTools

**MDTools** is a **Product Information Management** (PIM) platform dedicated to managing and organizing information on medical instruments such as finding instruments based on varying characteristics or outdated references. This project leverages a microservices architecture built with containers (Docker & OpenShift), including a SvelteKit frontend, a Spring Boot backend, and a PostgreSQL database.

## Table of Contents

[[_TOC_]]

## Project Structure

The project is organized as follows:

-   `/frontend` - Contains the SvelteKit frontend code.
-   `/backend` - Contains the Spring Boot backend API code.
-   `/database` - Contains the PostgreSQL database setup, including initialization scripts.

## Setup a development environment (Docker)

### Prerequisites

-   Docker and Docker Compose installed on your machine.
-   Access to the [MDTools Wiki](https://jira.montefiore.ulg.ac.be/xwiki/wiki/team0324/view/Main/) for additional setup details and project information.

### Installation

1.  **Clone the repository**:
```zsh
git clone git@gitlab.uliege.be:SPEAM/2024-2025/team3/mdtools.git
cd MDTools

```
2. **Build and Run Containers**: Use Docker Compose to build and start the containers.
```zsh
docker compose build
docker compose up -d
```
This command will:
- Build the SvelteKit frontend in /frontend
- Build the Spring Boot backend in /backend
- Set up the PostgreSQL database with any necessary initialization in /database

3. **Access the Application**:

```
Frontend: http://localhost:3000
API: http://localhost:8080/api
```

## Setup a production environment (OpenShift)

### Prerequisites
- Basic knowledge about OpenShift, see this [reference](https://jira.montefiore.ulg.ac.be/xwiki/wiki/openshiftcluster/view/Application%20Deployment/Deploy%20via%20the%20OpenShift%20Console/).

### Deployment
Import the YAML files in **/setup/openshift/** inside your OpenShift instance. Modify these files according to your image registry.

## Environment Variables

Ensure the following environment variables are set before running the application:

    JWT_SECRET - Secret key for JWT token generation.
    DB_USER - PostgreSQL database user.
    DB_PASSWORD - PostgreSQL database password.

Refer to the .env file.

## Authentication

**MDTools** uses JWT authorization for secure access to API endpoints. Each API request must include a valid JWT token in the request headers. Users must authenticate to obtain a token, which will be required for protected endpoints.

## API Documentation

The API specifications for MDTools can be found on Swagger: [MDTools API Documentation](https://app.swaggerhub.com/apis-docs/ALTTPCONH5R7Q/MDTools/).

## CI/CD Pipeline
This project uses [GitLab CI/CD](https://docs.gitlab.com/ee/ci/yaml/) for automated **building**, **testing**, and **deployment** of the **MDTools** components on OpenShift. The `.gitlab-ci.yml` file defines the following stages and jobs to manage the pipeline efficiently: 
- **Testing**: Runs unit tests for the frontend to ensure code quality. Test for the backend will be implemented in a near future.
- **Building**: Builds images for each component—-frontend, backend, and database—-and pushes them to the GitLab Container Registry.

### OpenShift-Specific Configurations

Each job that builds and pushes images uses OpenShift-compatible Docker configurations. Docker-in-Docker (`docker:20.10.16-dind`) is used to ensure isolated container builds,.