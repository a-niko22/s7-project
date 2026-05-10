FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY build/libs/*.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]