FROM openjdk:17-jdk-alpine

WORKDIR /app
COPY target/foodndeliv-0.0.1-SNAPSHOT.jar /app/foodndeliv.jar
#COPY opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar COMMENTED FOR TASK 1
EXPOSE 8080

# COMMENTED FOR TASK 1

#ENV JAVA_AGENT ""
#ENV OTEL_TRACES_EXPORTER "jaeger"
#ENV OTEL_EXPORTER_JAEGER_ENDPOINT "http://jaeger-all-in-one:14250"
#ENV OTEL_SERVICE_NAME "foodndeliv"
#ENV OTEL_TRACES_SAMPLER "always_on"

#ENTRYPOINT ["sh", "-c", "java $JAVA_AGENT -jar foodndeliv.jar"] COMMENTED FOR TASK 1
ENTRYPOINT ["java", "-jar", "foodndeliv.jar"]

#docker build -t markvellaum/foodndeliv:latest .
#docker push markvellaum/foodndeliv:latest

#Spring Boot Env variables and default values
#DB_HOST=localhost
#DB_PORT=5432
#DB_USER=myuser
#DB_PASSWORD
