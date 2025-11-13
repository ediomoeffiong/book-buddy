# ----------- Build Stage -----------
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

# Copy pom.xml and source code
COPY pom.xml .
COPY src src

# Build the app
RUN mvn clean package -DskipTests

# ----------- Run Stage -----------
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy JAR
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENV PORT=8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]