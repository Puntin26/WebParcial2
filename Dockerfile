# build stage
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /opt/app
COPY --from=build /app/target/eventos-academicos-1.0.0-jar-with-dependencies.jar app.jar
EXPOSE 7000
ENTRYPOINT ["java", "-jar", "app.jar"]
