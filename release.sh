#!/bin/bash

set -e

USERNAME=ryandkb8
IMAGE=tasks-backend

git checkout master
git pull

version=`git log --pretty=format:'%h' -n 1`

./build.sh

docker tag $USERNAME/$IMAGE:latest $USERNAME/$IMAGE:$version

docker push $USERNAME/$IMAGE:$version
docker push $USERNAME/$IMAGE:latest
