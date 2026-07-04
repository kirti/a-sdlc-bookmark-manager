# syntax=docker/dockerfile:1

# --- Build stage: produce the shadow (fat) JAR ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew --no-daemon shadowJar

# --- Runtime stage: run the Ktor HTTP server ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*-all.jar /app/app.jar
EXPOSE 8080
ENV PORT=8080
# Bind all interfaces inside the container (the container boundary provides isolation).
# Outside a container the server defaults to 127.0.0.1 — see Server.kt.
ENV BIND_HOST=0.0.0.0
# BOOKMARKS_FILE defaults to the running user's home; override at runtime to persist elsewhere.
# Entry point runs the server (ServerKt); the CLI (MainKt) can be run by overriding the command.
ENTRYPOINT ["java", "-cp", "/app/app.jar", "ServerKt"]
