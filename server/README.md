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

# Running on 1GB RAM vps

The server has been tested on STARDUST1-S 1GB RAM vps from Scaleway using CentOS 8.

Tutorial:
- Add your public key to your account [https://www.scaleway.com/en/docs/create-and-connect-to-your-server/](link).
- Create a STARDUST1-S 1GB instance with CentOS 8 image.
- Connect to the server with root user.  
- Run the script below to create a `server` user:

```bash
yum install -y yum-utils
yum-config-manager \
    --add-repo \
    https://download.docker.com/linux/centos/docker-ce.repo
yum install -y docker-ce docker-ce-cli containerd.io
systemctl enable docker
systemctl start docker
adduser server
usermod -aG docker server
mkdir /home/server/.ssh
cp -r /root/.ssh /home/server/.ssh
cp /root/.ssh/authorized_keys /home/server/.ssh/authorized_keys
chown -R server:server /home/server/.ssh
chmod 700 /home/server/.ssh
chmod 600 /home/server/.ssh/authorized_keys
curl -L "https://github.com/docker/compose/releases/download/1.29.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
yum install -y git

```
- Connect to the server through ssh using the new created `server` user.
- Clone the repo and enter into server folder:

```bash
git clone --recursive https://github.com/MapleStoryA/orion-server.git
cd orion-server

```
- Build the docker image

```bash
docker build -t orionms .
```

- Create a `.env` file with your info:

```
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=orion
PUBLIC_IP=51.51.51.51 # Add your server ip here
```
- Change the database password in `docker-compose.yml`
- Run `docker-compose up`
- Test the connectivity 

# MapleBit

The MapleBit installation will be available at port 8000.

To connect please use the host `orion-db` and use `SHA51` as encryption.

# All in one script

You can also try this script, which will do everything:

[Cloud init example](https://gist.githubusercontent.com/dilsonnn/bd3f66e4fde55b49cef2870d7f89ec28/raw/3a7c77afcd2688f93509783a3a29edb04ed71d79/cloud.init.sh)

# Connect

Run your game client pointing to your server ip on port `8484`


# Exposing the server from home
You can easily expose your server from home, if you have a small cloud server with nginx installed and ssh tunnels, here is an example:
```shell
cat /etc/nginx/nginx.conf
```
```
load_module '/usr/lib64/nginx/modules/ngx_stream_module.so';
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log;


events {
     worker_connections 1024;
}


stream {
    server {
        listen     8484;
        proxy_pass 127.0.0.1:8485;
    }
        server {
        listen     8585;
        proxy_pass 127.0.0.1:9585;
    }
        server {
        listen     8586;
        proxy_pass 127.0.0.1:9586;
    }
        server {
        listen     8587;
        proxy_pass 127.0.0.1:9587;
    }
        server {
        listen     8588;
        proxy_pass 127.0.0.1:9588;
    }
        server {
        listen     8589;
        proxy_pass 127.0.0.1:9589;
    }
        server {
        listen     8590;
        proxy_pass 127.0.0.1:9589;
    }
        server {
        listen     8799;
        proxy_pass 127.0.0.1:9799;
    }
}
```

And to create the tunnel: 
```shell
ssh <user>@<ip> -i <private key> -R 8485:localhost:8484 -R 9585:localhost:8585 -R 9586:localhost:8586 -R 9587:localhost:8587 -R 9588:localhost:8588 -R 9589:localhost:8589 -R 9590:localhost:8590 -R 9799:localhost:8799
```