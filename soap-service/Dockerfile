FROM maven:3.8-openjdk-11-slim
WORKDIR /soap-service
COPY . /soap-service
RUN mvn clean install -X
CMD ["mvn", "exec:java"]

