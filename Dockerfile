FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

ARG JAR_NAME
COPY build/libs/${JAR_NAME} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
