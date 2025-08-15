FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests -pl src/account-service -am clean package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/src/account-service/target/*-SNAPSHOT.jar app.jar
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 9001
ENTRYPOINT ["java","-jar","/app/app.jar"]
