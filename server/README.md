# Orion MS

## Running with a IDE

- Start the database with `docker-compose up orion-db`
- Run the main method in `GameApp` class.

## Runing as a container

### Install docker

- [Download Docker](https://docs.docker.com/docker-for-windows/install/)

### Build the image with jib

```shell
mvn jib:dockerBuild
```

### Start with compose

After building the image the server can be run with `docker-compose up`
