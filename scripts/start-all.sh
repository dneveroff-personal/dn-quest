#!/bin/bash

# DN Quest - Start All Services Script
# This script starts all DN Quest microservices and infrastructure

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
COMPOSE_FILE="docker-compose.yml"
ENVIRONMENT="dev"
PROJECT_NAME="dn-quest"
BUILD_IMAGES=false
PULL_IMAGES=false
VERBOSE=false

# Function to print colored output
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

# Function to show usage
show_usage() {
    cat << EOF
DN Quest - Start All Services

Usage: $0 [OPTIONS]

OPTIONS:
    -f, --file FILE         Docker compose file to use (default: docker-compose.yml)
    -e, --environment ENV   Environment to use (dev|prod|full) (default: dev)
    -p, --project NAME      Project name (default: dn-quest)
    -b, --build             Build images before starting
    --pull                  Pull latest images before starting
    -v, --verbose           Verbose output
    -h, --help              Show this help message

EXAMPLES:
    # Start development environment
    $0

    # Start with full monitoring stack
    $0 -e full -f docker-compose.full.yml

    # Start production environment with build
    $0 -e prod -f docker-compose.prod.yml -b

    # Start with custom project name
    $0 -p my-dn-quest

ENVIRONMENTS:
    dev     - Development environment with hot reload
    prod    - Production environment with optimizations
    full    - Full stack with monitoring and logging

EOF
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -f|--file)
            COMPOSE_FILE="$2"
            shift 2
            ;;
        -e|--environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -p|--project)
            PROJECT_NAME="$2"
            shift 2
            ;;
        -b|--build)
            BUILD_IMAGES=true
            shift
            ;;
        --pull)
            PULL_IMAGES=true
            shift
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Validate environment
case $ENVIRONMENT in
    dev|prod|full)
        ;;
    *)
        print_error "Invalid environment: $ENVIRONMENT. Must be one of: dev, prod, full"
        exit 1
        ;;
esac

# Set compose file based on environment if not specified
if [[ "$COMPOSE_FILE" == "docker-compose.yml" && "$ENVIRONMENT" != "dev" ]]; then
    case $ENVIRONMENT in
        prod)
            COMPOSE_FILE="docker-compose.prod.yml"
            ;;
        full)
            COMPOSE_FILE="docker-compose.full.yml"
            ;;
    esac
fi

# Check if compose file exists
if [[ ! -f "$COMPOSE_FILE" ]]; then
    print_error "Compose file not found: $COMPOSE_FILE"
    exit 1
fi

# Check if .env file exists for the environment
ENV_FILE=".env.${ENVIRONMENT}"
if [[ ! -f "$ENV_FILE" ]]; then
    if [[ -f ".env.example" ]]; then
        print_warning "Environment file $ENV_FILE not found. Copying from .env.example"
        cp .env.example "$ENV_FILE"
        print_warning "Please edit $ENV_FILE with your configuration"
    else
        print_warning "Environment file $ENV_FILE not found"
    fi
fi

# Set environment variables
export COMPOSE_PROJECT_NAME="$PROJECT_NAME"
export ENVIRONMENT="$ENVIRONMENT"

print_status "Starting DN Quest services..."
print_status "Environment: $ENVIRONMENT"
print_status "Compose file: $COMPOSE_FILE"
print_status "Project name: $PROJECT_NAME"

# Check Docker and Docker Compose
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed or not in PATH"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed or not in PATH"
    exit 1
fi

# Check Docker daemon
if ! docker info &> /dev/null; then
    print_error "Docker daemon is not running"
    exit 1
fi

# Pull images if requested
if [[ "$PULL_IMAGES" == true ]]; then
    print_status "Pulling latest images..."
    if [[ "$VERBOSE" == true ]]; then
        docker-compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" pull
    else
        docker-compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" pull --quiet
    fi
fi

# Build images if requested
if [[ "$BUILD_IMAGES" == true ]]; then
    print_status "Building images..."
    if [[ "$VERBOSE" == true ]]; then
        docker-compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" build
    else
        docker-compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" build --quiet
    fi
fi

# Start services
print_status "Starting services..."
if [[ "$VERBOSE" == true ]]; then
    docker-compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" up -d
else
    docker-compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" up -d --quiet
fi

# Wait for services to be healthy
print_status "Waiting for services to be healthy..."
sleep 10

# Check service health
print_status "Checking service health..."
UNHEALTHY_SERVICES=$(docker-compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" ps --filter "status=unhealthy" --format "table {{.Service}}")

if [[ -n "$UNHEALTHY_SERVICES" ]]; then
    print_warning "Some services are unhealthy:"
    echo "$UNHEALTHY_SERVICES"
    print_warning "Check logs with: docker-compose -f $COMPOSE_FILE --project-name $PROJECT_NAME logs [service-name]"
else
    print_success "All services are healthy!"
fi

# Show service status
print_status "Service status:"
docker-compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" ps

# Show access URLs
print_status "Services are accessible at:"
echo "  Frontend: http://localhost:3000"
echo "  API Gateway: http://localhost:8080"
echo "  API Documentation: http://localhost:8080/swagger-ui.html"

if [[ "$ENVIRONMENT" == "full" ]]; then
    echo "  Prometheus: http://localhost:9090"
    echo "  Grafana: http://localhost:3001"
    echo "  Jaeger: http://localhost:16686"
    echo "  Kibana: http://localhost:5601"
    echo "  Kafka UI: http://localhost:8089"
    echo "  MinIO Console: http://localhost:9001"
fi

print_success "DN Quest services started successfully!"
print_status "To view logs: docker-compose -f $COMPOSE_FILE --project-name $PROJECT_NAME logs -f"
print_status "To stop services: ./scripts/stop-all.sh -e $ENVIRONMENT -f $COMPOSE_FILE"