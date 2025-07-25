FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
EXPOSE 8080
