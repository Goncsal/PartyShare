#!/bin/bash

# TQS Project Docker Management Script

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Help function
show_help() {
    echo "TQS Project Docker Management Script"
    echo ""
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  dev         Start development environment (with H2 database)"
    echo "  prod        Start production environment (with PostgreSQL)"
    echo "  build       Build the Docker image"
    echo "  stop        Stop all containers"
    echo "  clean       Stop and remove all containers, networks, and volumes"
    echo "  logs        Show logs from all services"
    echo "  status      Show status of all services"
    echo "  test        Run tests in Docker container"
    echo "  help        Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 dev      # Start development environment"
    echo "  $0 prod     # Start production environment"
    echo "  $0 logs     # View logs"
    echo "  $0 clean    # Clean up everything"
}

# Development environment
start_dev() {
    print_status "Starting development environment..."
    docker compose -f docker-compose.dev.yml up --build -d
    print_success "Development environment started!"
    print_status "Application will be available at: http://localhost:8080"
    print_status "H2 Console available at: http://localhost:8080/h2-console"
    print_status "Debug port: 5005"
}

# Production environment
start_prod() {
    print_status "Starting production environment..."
    docker compose up --build -d
    print_success "Production environment started!"
    print_status "Application will be available at: http://localhost:8080"
    print_status "PostgreSQL available at: localhost:5432"
}

# Build images
build_images() {
    print_status "Building Docker images..."
    docker compose build
    print_success "Images built successfully!"
}

# Stop containers
stop_containers() {
    print_status "Stopping all containers..."
    docker compose -f docker-compose.yml down
    docker compose -f docker-compose.dev.yml down
    print_success "All containers stopped!"
}

# Clean up everything
clean_all() {
    print_warning "This will remove all containers, networks, and volumes!"
    read -p "Are you sure? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_status "Cleaning up..."
        docker compose -f docker-compose.yml down -v --remove-orphans
        docker compose -f docker-compose.dev.yml down -v --remove-orphans
        docker system prune -f
        print_success "Cleanup completed!"
    else
        print_status "Cleanup cancelled."
    fi
}

# Show logs
show_logs() {
    print_status "Showing logs (Ctrl+C to exit)..."
    docker compose logs -f
}

# Show status
show_status() {
    print_status "Container status:"
    docker compose ps
    echo ""
    print_status "Development containers:"
    docker compose -f docker-compose.dev.yml ps
}

# Run tests
run_tests() {
    print_status "Running tests in Docker container..."
    docker compose -f docker-compose.dev.yml exec tqs-backend-dev ./mvnw test
}

# Main logic
case "${1:-help}" in
    "dev")
        start_dev
        ;;
    "prod")
        start_prod
        ;;
    "build")
        build_images
        ;;
    "stop")
        stop_containers
        ;;
    "clean")
        clean_all
        ;;
    "logs")
        show_logs
        ;;
    "status")
        show_status
        ;;
    "test")
        run_tests
        ;;
    "help"|*)
        show_help
        ;;
esac
