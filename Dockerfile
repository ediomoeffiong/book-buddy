# ----------- Build Stage -----------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy only pom.xml first to leverage caching
COPY pom.xml .
RUN mvn dependency:go-offline

# Now copy source
COPY src ./src

# Build application
RUN mvn clean package -DskipTests


# ----------- Run Stage -----------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENV PORT=8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]