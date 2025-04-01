# Popcorn Palace

Popcorn Palace is a Java Spring Boot project for managing a cinema system. It provides functionality for handling movies, showtimes, and ticket bookings, including robust business logic and data validations.

## Technologies Used

- Java 17
- Spring Boot
- Maven
- JPA (Hibernate)
- H2 / PostgreSQL
- RESTful APIs
- JUnit (unit and integration testing)

## Features

- Create, update, delete, and retrieve movies
- Manage showtimes with time validation and conflict checks
- Prevent deletion of movies or showtimes that are already linked to tickets
- Purchase and manage tickets with seat validation
- Custom exception handling and centralized error responses
- Data initialization with SQL scripts

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

## run
1. start the database:
   `Install PostgreSQL/MySQL`
2. start the app:
   `mvn spring-boot:run`
3.run 'docker-compose up'
4. The app will run on port 8080. To change that, modify the server.port setting in the application.yml file.

## Testing
1. `mvn test`

