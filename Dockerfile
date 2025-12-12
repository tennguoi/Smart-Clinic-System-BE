# Build stage
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY uploads ./uploads

# Create uploads directory if it doesn't exist
RUN mkdir -p uploads/photos

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
docker-compose.yml