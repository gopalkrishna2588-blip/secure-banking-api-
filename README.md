# Secure Banking API

A Banking Backend built with Java 17 and Spring Boot.

## How to Run
mvn spring-boot:run

## Swagger UI
http://localhost:8080/swagger-ui.html

## API Endpoints
- POST /api/auth/register
- POST /api/auth/login
- POST /api/accounts
- GET  /api/accounts/{id}
- POST /api/accounts/{id}/deposit
- POST /api/accounts/{id}/withdraw
- POST /api/accounts/transfer
- GET  /api/transactions/{accountId}

## Tech Stack
- Java 17
- Spring Boot 3.2.3
- Spring Security + JWT
- Spring Data JPA
- H2 Database
- Swagger/OpenAPI
- Lombok
- JUnit 5 + Mockito
