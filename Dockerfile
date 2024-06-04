FROM openjdk:21-jdk
EXPOSE 8080
COPY build/libs/filesync-server-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]