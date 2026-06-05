FROM maven:3.9.15-eclipse-temurin-26 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd -r app && useradd -r -g app app
COPY --from=build /workspace/target/mindarena-config-server-0.1.0.jar app.jar
USER app
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
