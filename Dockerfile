# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# Run stage
FROM eclipse-temurin:17-jre
RUN apt-get update && apt-get install -y stockfish && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV STOCKFISH_PATH=/usr/games/stockfish
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
