# syntax=docker/dockerfile:1

# ---- Build Stage ----
FROM gradle:8.5-jdk21 AS build

# Set workdir
WORKDIR /app

# Copy Gradle wrapper and build scripts first for better caching
COPY --link build.gradle.kts settings.gradle.kts gradlew gradlew.bat ./
COPY --link gradle gradle/

# Download dependencies (will be cached if unchanged)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# Copy the rest of the source code
COPY --link src src
COPY --link .kotlin .kotlin

# Build the application (skip tests for faster build)
RUN ./gradlew build --no-daemon -x test

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create a non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Set permissions
RUN chown appuser:appgroup app.jar

USER appuser

# Expose the default Spring Boot port (adjust if needed)
EXPOSE 8080

# JVM options: container-aware, memory limits
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
