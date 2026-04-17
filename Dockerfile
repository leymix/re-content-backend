FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S recontent && adduser -S recontent -G recontent
COPY --from=build /workspace/target/*.jar app.jar
USER recontent
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
