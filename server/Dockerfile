FROM maven:3.8.1-openjdk-8-slim
WORKDIR /orion
COPY ./src ./src
COPY ./pom.xml /orion/pom.xml
COPY ./start.sh /orion/start.sh
RUN mvn clean install
CMD ["./start.sh"]