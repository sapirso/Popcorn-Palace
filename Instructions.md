# Instructions

## Prerequisites
Java 17 or higher
Maven 3.8+
Docker (optional, for database)

## Dependencise
1. Download and install from Oracle Java or OpenJDK
   Verify installation: java -version
2. Download from Apache Maven
   Add to system PATH
   Verify installation: mvn -version

## Run with Docker
1.run 'docker-compose up'

## run
1. start the database:
   `Install PostgreSQL/MySQL`
2. start the app:
   `./mvnw spring-boot:run`
3. The app will run on port 8080.
   $ curl http://localhost:8080
   Greetings from Spring Boot!

## Testing
1. `mvn test`
