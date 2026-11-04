FROM eclipse-temurin:25-alpine
WORKDIR /app
EXPOSE 8080
ARG JAR_FILE=target/challengeApi-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]