# ----------- Build Stage -----------
FROM maven:3.9.5-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src src

# Build the app (skip tests)
RUN mvn clean package -DskipTests

# ----------- Run Stage -----------
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose Render port
EXPOSE 8080
ENV PORT=8080

# Start the app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]