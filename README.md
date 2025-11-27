## Running with Docker

This project includes Docker and Docker Compose support for easy setup and deployment.

### Requirements
- Docker
- Docker Compose

### Build and Run

To build and start the application using Docker Compose:

```sh
docker-compose up --build
```

This will build the application using Gradle 8.5 with JDK 17 and run it on Eclipse Temurin 17 JRE (Alpine). The application will be available on port **8080**.

### Configuration
- The application exposes port **8080** by default.
- If you need to set environment variables, you can use a `.env` file in the project root. Uncomment the `env_file` line in `docker-compose.yml` if needed.
- No external services (like databases) are configured by default. If your setup requires additional services, update `docker-compose.yml` accordingly.

### Notes
- The container runs as a non-root user for security.
- JVM options are set for container environments.

For any custom configuration, refer to the `docker-compose.yml` and `Dockerfile` in the project root.