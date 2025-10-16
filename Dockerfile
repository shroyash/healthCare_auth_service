FROM eclipse-temurin:21-jdk-jammy

# Set working directory
WORKDIR /app

# Copy Maven built JAR into container
COPY target/auth-service-0.0.1-SNAPSHOT.jar app.jar

# Expose application port and debug port
EXPOSE 8003 5005

# Run the JAR with remote debugging enabled, suspend=n to allow immediate start
ENTRYPOINT ["java","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005","-jar","app.jar"]
