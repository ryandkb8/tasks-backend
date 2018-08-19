[![Build Status](https://travis-ci.com/ryandkb8/tasks-backend.svg?branch=master)](https://travis-ci.com/ryandkb8/tasks-backend)

# Backend for tasks service

This service provides an API for basic CRUD operations relating to tasks. Postgresql is used as the data store.

## Continuous Integration 
Creating a pull-request will trigger a build in traivs-ci

## Bringing up local instance
To bring up a local instance of the backend and postgresql database run the following:  `docker-compose up`

## Running tests
In order to run tests you must have Java 8 and SBT installed as well as docker running.

A docker container with the postgresql database must be running in order to run tests. This can be brought up with by running `./script_db.sh`

To bring up the database and run all the tests run the following:
```
./script_db.sh
sbt clean test compile
```


## Releasing
To build the docker image and push it to docker hub run the following: `./release.sh`
The image is versioned by the shorted git hash