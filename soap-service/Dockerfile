FROM maven:3.8-openjdk-11-slim

WORKDIR /app

COPY /src ./src
COPY pom.xml .
COPY target ./target
COPY .env /app/.env

RUN mvn clean install -X

CMD ["mvn", "exec:java"]