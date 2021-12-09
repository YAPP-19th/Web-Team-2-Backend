FROM openjdk:13-jdk-slim
EXPOSE 8080
ARG JAR_FILE=/build/libs/Web-Team-2-Backend-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]