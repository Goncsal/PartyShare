# Load Testing Guide

This guide explains how to run load tests using the provided Docker Compose setup.

## Prerequisites
- Docker and Docker Compose installed.
- Your backend application running on port 8080 (or update `test.js` to point to the correct URL).

## Part 1: Infrastructure Setup

To start the infrastructure (k6, Prometheus, Grafana), run:

```bash
cd load-tests
docker-compose up -d
```

This will start:
- **Prometheus**: http://localhost:9091
- **Grafana**: http://localhost:3000

## Part 2: Running Tests

The `k6` service is configured to run the `test.js` script automatically when the container starts. However, since `docker-compose up -d` starts it in the background, you might want to run it explicitly or view logs.

To run the test manually from inside the container:

```bash
docker-compose run --rm k6 run -o experimental-prometheus-rw /scripts/test.js
```

Or if you just want to see the logs of the auto-started run:

```bash
docker-compose logs -f k6
```

## Part 3: Visualization (Grafana)

1. Open Grafana at [http://localhost:3000](http://localhost:3000).
2. Login with default credentials:
   - **Username**: `admin`
   - **Password**: `admin` (You may be asked to change it, or skip).
3. Verify data source:
   - Go to **Configuration** -> **Data Sources**.
   - You should see **Prometheus** configured.
4. Dashboards:
   - Go to **Dashboards** -> **Manage**.
   - You can import a k6 dashboard (ID `19665` is a popular one) or create your own.
   - *Note: If you added a provisioning file for dashboards, check the "General" or "k6" folder.*

## Part 4: Web Report

After the test completes, a `summary.html` report will be generated in the `load-tests/scripts` directory.
You can open this file in your browser to see the results.

## Cleanup

To stop and remove the containers:

```bash
docker-compose down
```
