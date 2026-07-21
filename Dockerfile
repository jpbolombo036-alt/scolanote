# Étape 1 : Build
FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# Étape 2 : Exécution
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
COPY entrypoint.sh entrypoint.sh
RUN chmod +x entrypoint.sh && apk add --no-cache netcat-openbsd

ENV PORT=8000
EXPOSE 8000

ENTRYPOINT ["sh", "entrypoint.sh"]
