#!/bin/bash

# TQS Backend Test Runner Script
echo "=== TQS Backend Test Runner ==="
echo "Running all tests for the backend"

cd tqsbackend

# Make sure mvnw is executable
chmod +x ./mvnw

# Clean and run tests with coverage
echo "Running tests with coverage report (jacoco)"
./mvnw clean test jacoco:report

# Check exit code
if [ $? -eq 0 ]; then
    echo "All tests passed"
    echo "Coverage report generated at: target/site/jacoco/index.html"
    echo "Test reports available at: target/surefire-reports/"
else
    echo "Some tests failed"
    exit 1
fi
