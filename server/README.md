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
git clone https://github.com/MapleStoryA/orion-server.git
cd orion-server

```
- Build the docker image

```bash
docker build -t orionms .
```

- Edit `docker-compose.yml` to your public address:

```diff
     container_name: orion-server
     image: orionms
     extra_hosts:
-      orion-public: 127.0.0.1
+      orion-public:  <vps public ipv4 ip>
```
- Change the database password in `docker-compose.yml`
- Run `docker-compose up`
- Test the connectivity 

# All in one script

You can also try this script, which will do everything:

[Cloud init example](https://gist.githubusercontent.com/dilsonnn/bd3f66e4fde55b49cef2870d7f89ec28/raw/f83bcf09d89c88ec9ef41a0981a4b03e68c0c799/cloud.init.sh)

# Connect

Run your game client pointing to your server ip on port `8484`