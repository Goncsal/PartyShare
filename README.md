# TQS Project

This is a TQS project.

Below are instructions for the CI pipeline and how to run the tests (and proj structure)

## Project Structure

- `tqsbackend/` - Spring Boot backend application
- `docs/` - Project documentation

## Backend (Spring Boot)

The backend is built with:
- Java 21
- Spring Boot 3.5.7
- Spring Data JPA
- Spring Boot Actuator
- Thymeleaf (for frontend templating)
- H2 Database (for development)
- PostgreSQL (for production)
- Maven for build management

### Quick Start with Docker

The easiest way to run the application is using the provided Docker setup:

```bash
# Development environment (H2 database, hot reload, debug port)
./docker.sh dev

# Production environment (PostgreSQL, optimized)
./docker.sh prod

# View logs
./docker.sh logs

# Stop all containers
./docker.sh stop

# Clean up everything
./docker.sh clean
```

### Manual Setup

#### Running the Backend Locally

```bash
cd tqsbackend
./mvnw spring-boot:run
```

#### Running Tests

```bash
cd tqsbackend
./mvnw test
```

#### Building the Application

```bash
cd tqsbackend
./mvnw clean package
```

### Docker Environments

#### Development Environment
- **URL**: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console
- **SonarQube**: http://localhost:9000 (admin/admin)
- **Debug Port**: 5005
- **Features**: Hot reload, detailed logging, all actuator endpoints enabled

#### Production Environment
- **URL**: http://localhost:8080
- **Database**: PostgreSQL (localhost:5432)
- **SonarQube**: http://localhost:9000 (admin/admin)
- **Features**: Optimized for performance and security

### Available Endpoints

- `/` - Home page (Thymeleaf template)
- `/health` - Custom health page
- `/actuator/health` - Spring Boot health endpoint
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics
- `/h2-console` - H2 database console (dev profile only)

### Code Quality with SonarQube

Run SonarQube analysis:

```bash
# Start environment with SonarQube
./docker.sh dev  # or ./docker.sh prod

# Run analysis
./docker.sh sonar
```

Access SonarQube dashboard at http://localhost:9000

For detailed SonarQube setup and CI integration, see [SONARQUBE_SETUP.md](SONARQUBE_SETUP.md)

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
