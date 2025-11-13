# Use lightweight Java 17 image
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the app (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

# ----------- Run Stage -----------
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose Render port
EXPOSE 8080

# Use the PORT env var from Render
ENV PORT=8080

# Start the app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]