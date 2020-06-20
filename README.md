# Orion Server V90
A Maple Story V90 Emulator

# Client file:
https://mega.nz/file/qIABEAaa#tber4CWFTLv6iwBf2klEQO1g9rYfFl3EIrNP2BjsyjA

# Required software for development and runtime: 
- JDK 8/11 installed and available in PATH
- MySQL 5.6 installed and available in PATH
- Intelij Community Edition
- Visual Studio 2015
- Maven installed and available in PATH

# Installing the database:
- Download the MySQL Server.
- Open a CMD window in server/database 
- Make sure that the password and login is `root` or change in database.properties 
- Run:
```
 > mysql -u root -p
 > CREATE SCHEMA orionv90;
 > USE orionv90;
 > SOURCE ./schema.sql;
 > EXIT;
```

# Running the server:
- Make sure that the SQL Server is running in port 3306.
- Navigate to server folder.
- Run:
```
> build.bat
> start.bat
```

# Launching the game
- Compile the launcher and the client projects in Visual Studio 2015
- Copy the client.dll and Launcher.exe into your MapleStory folder
- Create a file named Server.txt with the following content:
```
MapleStory.exe
GameLaunching 127.0.0.1 8484
```
- Start the game
