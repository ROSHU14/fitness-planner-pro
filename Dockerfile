FROM amazoncorretto:17-alpine

WORKDIR /app

# Copy from the fitnessplanner subfolder
COPY fitnessplanner/.mvn .mvn
COPY fitnessplanner/mvnw .
COPY fitnessplanner/pom.xml .
COPY fitnessplanner/src src

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Find and copy the jar file to a known location
RUN mkdir -p target && cp fitnessplanner/target/*.jar target/app.jar || cp target/*.jar target/app.jar

EXPOSE 8080

CMD ["java", "-jar", "target/app.jar"]