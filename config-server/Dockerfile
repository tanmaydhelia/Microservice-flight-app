FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy the build artifact (ensure you run 'mvn clean package' first)
COPY target/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]