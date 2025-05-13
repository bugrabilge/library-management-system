FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY . .
RUN ./gradlew build -x test --no-daemon
CMD ["java", "-jar", "build/libs/library-management-system-0.0.1-SNAPSHOT.jar"]
