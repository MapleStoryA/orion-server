FROM maven:3.8.1-openjdk-8-slim
WORKDIR /orion
COPY ./src ./src
COPY ./pom.xml ./pom.xml
COPY ./start.sh ./start.sh
RUN mvn clean install
CMD ["./start.sh"]