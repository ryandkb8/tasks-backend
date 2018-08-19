#!/bin/bash

set -e

docker pull ryandkb8/tasks-postgresql:latest
docker run -d -p 5432:5432 ryandkb8/tasks-postgresql:latest
