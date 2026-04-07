#!/bin/bash

# DN Quest - Restart Service Script
# This script restarts a specific service in the DN Quest microservices architecture

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
SERVICE_NAME=""
REBUILD=false
VERBOSE=false
FOLLOW_LOGS=false

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
DN Quest - Restart Service

Usage: $0 [OPTIONS] SERVICE_NAME

OPTIONS:
    -f, --file FILE         Docker compose file to use (default: docker-compose.yml)
    -e, --environment ENV   Environment to use (dev|prod|full) (default: dev)
    -p, --project NAME      Project name (default: dn-quest)
    -b, --build             Rebuild the service image before restarting
    -l, --logs              Follow logs after restart
    -v, --verbose           Verbose output
    -h, --help              Show this help message

SERVICE_NAME:
    The name of the service to restart. Available services:
    - api-gateway
    - authentication-service
    - user-management-service
    - quest-management-service
    - game-engine-service
    - team-management-service
    - notification-service
    - statistics-service
    - file-storage-service
    - frontend
    - postgres
    - redis
    - kafka
    - zookeeper
    - minio
    - nginx
    - prometheus
    - grafana
    - jaeger
    - elasticsearch
    - kibana
    - logstash

EXAMPLES:
    # Restart authentication service
    $0 authentication-service

    # Restart API gateway with rebuild
    $0 api-gateway -b

    # Restart frontend in production environment
    $0 -e prod -f docker-compose.prod.yml frontend

    # Restart service and follow logs
    $0 notification-service -l

    # Restart with custom project name
    $0 -p my-dn-quest postgres

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
            REBUILD=true
            shift
            ;;
        -l|--logs)
            FOLLOW_LOGS=true
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
        -*)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
        *)
            if [[ -z "$SERVICE_NAME" ]]; then
                SERVICE_NAME="$1"
            else
                print_error "Multiple service names provided: $SERVICE_NAME and $1"
                show_usage
                exit 1
            fi
            shift
            ;;
    esac
done

# Check if service name is provided
if [[ -z "$SERVICE_NAME" ]]; then
    print_error "Service name is required"
    show_usage
    exit 1
fi

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

# Set environment variables
export COMPOSE_PROJECT_NAME="$PROJECT_NAME"
export ENVIRONMENT="$ENVIRONMENT"

print_status "Restarting DN Quest service..."
print_status "Service: $SERVICE_NAME"
print_status "Environment: $ENVIRONMENT"
print_status "Compose file: $COMPOSE_FILE"
print_status "Project name: $PROJECT_NAME"

# Check Docker and Docker Compose
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed or not in PATH"
    exit 1
fi

if ! command -v docker compose &> /dev/null; then
    print_error "Docker Compose is not installed or not in PATH"
    exit 1
fi

# Check if service exists in compose file
if ! docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" config --services | grep -q "^${SERVICE_NAME}$"; then
    print_error "Service '$SERVICE_NAME' not found in compose file: $COMPOSE_FILE"
    
    print_status "Available services:"
    docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" config --services | sort
    exit 1
fi

# Check if service is running
SERVICE_STATUS=$(docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" ps "$SERVICE_NAME" --format "table {{.Status}}" | tail -n +2)

if [[ -z "$SERVICE_STATUS" ]]; then
    print_warning "Service '$SERVICE_NAME' is not currently running"
    print_status "Starting service..."
else
    print_status "Current service status: $SERVICE_STATUS"
fi

# Build service if requested
if [[ "$REBUILD" == true ]]; then
    print_status "Rebuilding service image..."
    if [[ "$VERBOSE" == true ]]; then
        docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" build "$SERVICE_NAME"
    else
        docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" build "$SERVICE_NAME" --quiet
    fi
fi

# Restart service
print_status "Restarting service: $SERVICE_NAME"
if [[ "$VERBOSE" == true ]]; then
    docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" restart "$SERVICE_NAME"
else
    docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" restart "$SERVICE_NAME" --timeout 30
fi

# Wait for service to be healthy
print_status "Waiting for service to be healthy..."
sleep 5

# Check service health
HEALTH_STATUS=$(docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" ps "$SERVICE_NAME" --format "table {{.Status}}" | tail -n +2)

if [[ "$HEALTH_STATUS" == *"healthy"* ]]; then
    print_success "Service '$SERVICE_NAME' is healthy!"
elif [[ "$HEALTH_STATUS" == *"running"* ]]; then
    print_success "Service '$SERVICE_NAME' is running!"
else
    print_warning "Service status: $HEALTH_STATUS"
    print_warning "Check logs for more information"
fi

# Show service status
print_status "Service status:"
docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" ps "$SERVICE_NAME"

# Follow logs if requested
if [[ "$FOLLOW_LOGS" == true ]]; then
    print_status "Following logs for service: $SERVICE_NAME"
    echo "Press Ctrl+C to stop following logs"
    docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" logs -f "$SERVICE_NAME"
fi

print_success "Service '$SERVICE_NAME' restarted successfully!"
print_status "To view logs: docker compose -f $COMPOSE_FILE --project-name $PROJECT_NAME logs -f $SERVICE_NAME"
print_status "To view all services: docker compose -f $COMPOSE_FILE --project-name $PROJECT_NAME ps"