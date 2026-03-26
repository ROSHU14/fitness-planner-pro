FROM amazoncorretto:17-alpine

WORKDIR /app

# Copy from the fitnessplanner subfolder
COPY fitnessplanner/.mvn .mvn
COPY fitnessplanner/mvnw .
COPY fitnessplanner/pom.xml .
COPY fitnessplanner/src src

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/*.jar"]