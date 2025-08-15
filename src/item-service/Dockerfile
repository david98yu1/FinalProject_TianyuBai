# ---- Build stage ----
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
# Build only this service and what it needs (-am builds deps like common-lib)
RUN mvn -q -DskipTests -pl src/item-service -am clean package

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/src/item-service/target/*-SNAPSHOT.jar app.jar
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 9002
ENTRYPOINT ["java","-jar","/app/app.jar"]
