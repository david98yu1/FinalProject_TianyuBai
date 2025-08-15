# syntax=docker/dockerfile:1.6

FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /workspace

# bring in the whole multi-module project (parent pom + modules)
COPY pom.xml .
COPY src ./src

# build only account-service and required modules (-am)
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -DskipTests -pl src/account-service -am clean package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# copy the built jar (don’t assume SNAPSHOT)
COPY --from=build /workspace/src/account-service/target/*.jar app.jar
# uncomment this only if you have application-docker.properties
# ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 9001
ENTRYPOINT ["java","-jar","/app/app.jar"]
