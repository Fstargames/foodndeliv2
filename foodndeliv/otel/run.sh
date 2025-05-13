#!/bin/bash

#wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.27.0/opentelemetry-javaagent.jar

export OTEL_TRACES_EXPORTER=jaeger
export OTEL_EXPORTER_JAEGER_ENDPOINT=http://localhost:14250
export OTEL_SERVICE_NAME=my-spring-boot-app
export OTEL_TRACES_SAMPLER=always_on

java -javaagent:./opentelemetry-javaagent.jar \
     -Dotel.traces.exporter=jaeger \
     -Dotel.exporter.jaeger.endpoint=http://localhost:14250 \
     -Dotel.service.name=foodndeliv \
     -Dotel.traces.sampler=always_on \
     -jar foodndeliv-0.0.1-SNAPSHOT.jar