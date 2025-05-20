# Dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
ARG JAR_FILE=target/foodndeliv-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# Copy the OpenTelemetry Java Agent from the 'otel-agent' directory in the build context
COPY otel/opentelemetry-javaagent.jar ./opentelemetry-javaagent.jar

EXPOSE 8080
ENTRYPOINT ["java", "-javaagent:./opentelemetry-javaagent.jar", "-jar", "app.jar"]