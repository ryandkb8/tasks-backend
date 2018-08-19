#!/bin/bash

set -e

USERNAME=ryandkb8
IMAGE=tasks-backend

docker build -t $USERNAME/$IMAGE:latest .