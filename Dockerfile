# Use lightweight Java 17 image
FROM eclipse-temurin:17-jdk-alpine AS build

# Set working directory inside the container
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies (for caching)
# RUN ./mvnw dependency:go-offline -B
# RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Build the app (skip tests for faster build)
# RUN ./mvnw clean package -DskipTests
# RUN ./mvnw clean package

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