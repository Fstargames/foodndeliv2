#!/bin/bash

#wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.27.0/opentelemetry-javaagent.jar

export DB_HOST=localhost
export DB_PORT=5432
export DB_Name=ordersdb
export DB_USER=myadmin
export DB_PASSWORD=mypass # Crucial

export OTEL_TRACES_EXPORTER=jaeger
export OTEL_EXPORTER_JAEGER_ENDPOINT=http://localhost:14250
export OTEL_SERVICE_NAME=foodndeliv-local
export OTEL_TRACES_SAMPLER=always_on

java -javaagent:./opentelemetry-javaagent.jar \
     -Dotel.service.name=${OTEL_SERVICE_NAME} \
     -Dotel.traces.exporter=${OTEL_TRACES_EXPORTER} \
     -Dotel.exporter.jaeger.endpoint=${OTEL_EXPORTER_JAEGER_ENDPOINT} \
     -Dotel.traces.sampler=${OTEL_TRACES_SAMPLER} \
     -Dotel.metrics.exporter=none \
     -Dotel.logs.exporter=none \
     -jar ../target/foodndeliv-0.0.1-SNAPSHOT.jar
