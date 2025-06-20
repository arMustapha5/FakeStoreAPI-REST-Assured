FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src
COPY notify.sh .
RUN chmod +x notify.sh

CMD ["sh", "-c", "mvn clean test allure:report 2>&1 | tee maven_output.log; MAVEN_EXIT=$?; ./notify.sh; exit $MAVEN_EXIT"]


