# TQS Project

[![CI/CD Pipeline](https://github.com/Goncsal/tqs_project/actions/workflows/ci.yml/badge.svg)](https://github.com/Goncsal/tqs_project/actions/workflows/ci.yml)
[![Code Quality](https://github.com/Goncsal/tqs_project/actions/workflows/code-quality.yml/badge.svg)](https://github.com/Goncsal/tqs_project/actions/workflows/code-quality.yml)

This is a TQS (Testing and Quality Software) project featuring a Spring Boot backend application.

## Project Structure

- `tqsbackend/` - Spring Boot backend application
- `docs/` - Project documentation

## Backend (Spring Boot)

The backend is built with:
- Java 21
- Spring Boot 3.5.7
- Spring Data JPA
- H2 Database (for development)
- Maven for build management

### Running the Backend

```bash
cd tqsbackend
./mvnw spring-boot:run
```

### Running Tests

```bash
cd tqsbackend
./mvnw test
```

### Building the Application

```bash
cd tqsbackend
./mvnw clean package
```

## CI/CD

This project uses GitHub Actions for continuous integration. The pipeline:

1. **Build & Test Pipeline** (`ci.yml`):
   - Runs on push to `main`, `develop`, and `feature/**` branches
   - Runs on pull requests to `main` and `develop`
   - Compiles the code
   - Runs all tests
   - Packages the application
   - Uploads test results and JAR artifacts

2. **Code Quality Pipeline** (`code-quality.yml`):
   - Runs Checkstyle for code style verification
   - Runs SpotBugs for static code analysis

All tests must pass before code can be merged into the main branches.
