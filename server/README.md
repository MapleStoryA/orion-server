# Maple Story Orion V90

## Installing Docker
- [Download Docker](https://docs.docker.com/docker-for-windows/install/)

## Set your server database password

For now just change in `docker-compose.yml` and make it's not exposed to the world!

# Building the image:
```bash
docker build -t orionms .
```

# Running the server

Configure `docker-compose.yml` `orion-public` to your public ip address(or use 127.0.0.1 for localhost) and run:
```bash
docker-compose up
```

# Connect
Run your localhost client pointing to your server ip on port `8484`