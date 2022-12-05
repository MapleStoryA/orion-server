# Maple Story Orion V90

## Run locally with Intellij

Once the database is started with `docker-compose up orion-db`, the app can be started by running the `GameApp` class.

## Installing Docker

- [Download Docker](https://docs.docker.com/docker-for-windows/install/)

# Building the docker image

```shell
mvn jib:dockerBuild
```

# Running with docker compose

After building the image with jib, the server should run with `docker-compose up`
