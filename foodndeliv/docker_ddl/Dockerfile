FROM openjdk:17-jdk-alpine

WORKDIR /app
COPY target/foodndeliv-0.0.1-SNAPSHOT.jar /app/foodndeliv.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "foodndeliv.jar"]

#docker build -t markvellaum/foodndeliv:ddl .
#docker push markvellaum/foodndeliv:ddl

#Spring Boot Env variables and default values
#DB_HOST=localhost
#DB_PORT=5432
#DB_USER=admin
#DB_PASSWORD
