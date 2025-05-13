#!/bin/bash

# Set environment variables for PostgreSQL
export POSTGRES_USER=myadmin
export POSTGRES_PASSWORD=mypass
export POSTGRES_DB=ordersdb
export POSTGRES_PORT=5432
export POSTGRES_HOST=localhost

# Echo back settings
echo "Starting PostgreSQL with the following environment variables:"
echo "POSTGRES_USER=$POSTGRES_USER"
echo "POSTGRES_PASSWORD=$POSTGRES_PASSWORD"
echo "POSTGRES_DB=$POSTGRES_DB"
echo "POSTGRES_PORT=$POSTGRES_PORT"
echo "POSTGRES_HOST=$POSTGRES_HOST"

# Run PostgreSQL Docker container
docker run --name postgres-ordersdb \
  -e POSTGRES_USER=$POSTGRES_USER \
  -e POSTGRES_PASSWORD=$POSTGRES_PASSWORD \
  -e POSTGRES_DB=$POSTGRES_DB \
  -p $POSTGRES_PORT:5432 \
  -dit postgres

# Prompt start message
echo "PostgreSQL starting ..."