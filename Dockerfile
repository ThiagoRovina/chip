FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY server-api/gradlew server-api/gradlew
COPY server-api/gradle server-api/gradle
COPY server-api/build.gradle server-api/settings.gradle server-api/
COPY server-api/src server-api/src

WORKDIR /app/server-api

RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/server-api/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
