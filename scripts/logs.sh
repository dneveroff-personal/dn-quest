#!/bin/bash

# DN Quest - Logs Viewer Script
# This script displays logs for DN Quest microservices and infrastructure

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Default values
COMPOSE_FILE="docker-compose.yml"
ENVIRONMENT="dev"
PROJECT_NAME="dn-quest"
SERVICE_NAME=""
FOLLOW=false
TAIL_LINES=100
SINCE=""
TIMESTAMP=false
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

print_header() {
    echo -e "${CYAN}=== $1 ===${NC}"
}

# Function to show usage
show_usage() {
    cat << EOF
DN Quest - Logs Viewer

Usage: $0 [OPTIONS] [SERVICE_NAME]

OPTIONS:
    -f, --file FILE         Docker compose file to use (default: docker-compose.yml)
    -e, --environment ENV   Environment to use (dev|prod|full) (default: dev)
    -p, --project NAME      Project name (default: dn-quest)
    -f, --follow            Follow log output (like tail -f)
    -t, --tail LINES        Number of lines to show from the end (default: 100)
    -s, --since DURATION    Show logs since timestamp (e.g., 2h, 30m, 1d)
    --timestamps            Show timestamps
    -v, --verbose           Verbose output
    -h, --help              Show this help message

SERVICE_NAME:
    The name of the service to view logs for. If not provided, shows logs for all services.
    Available services:
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
    # View logs for all services
    $0

    # Follow logs for authentication service
    $0 authentication-service -f

    # View last 50 lines for API gateway
    $0 api-gateway -t 50

    # View logs from last 2 hours for all services
    $0 -s 2h

    # View logs with timestamps for production environment
    $0 -e prod -f docker-compose.prod.yml --timestamps

    # View logs for multiple services
    $0 postgres redis kafka -f

EOF
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -f|--file)
            if [[ "$2" != -* && -n "$2" ]]; then
                COMPOSE_FILE="$2"
                shift 2
            else
                FOLLOW=true
                shift
            fi
            ;;
        -e|--environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -p|--project)
            PROJECT_NAME="$2"
            shift 2
            ;;
        -t|--tail)
            TAIL_LINES="$2"
            shift 2
            ;;
        -s|--since)
            SINCE="$2"
            shift 2
            ;;
        --timestamps)
            TIMESTAMP=true
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
            SERVICE_NAMES+=("$1")
            shift
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

# Set environment variables
export COMPOSE_PROJECT_NAME="$PROJECT_NAME"
export ENVIRONMENT="$ENVIRONMENT"

print_status "DN Quest Logs Viewer"
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

# Build logs command
LOGS_CMD="docker compose -f $COMPOSE_FILE --project-name $PROJECT_NAME logs"

if [[ "$FOLLOW" == true ]]; then
    LOGS_CMD="$LOGS_CMD -f"
fi

if [[ -n "$TAIL_LINES" ]]; then
    LOGS_CMD="$LOGS_CMD --tail=$TAIL_LINES"
fi

if [[ -n "$SINCE" ]]; then
    LOGS_CMD="$LOGS_CMD --since=$SINCE"
fi

if [[ "$TIMESTAMP" == true ]]; then
    LOGS_CMD="$LOGS_CMD --timestamps"
fi

# If no service names provided, show all services
if [[ ${#SERVICE_NAMES[@]} -eq 0 ]]; then
    print_status "Showing logs for all services..."
    
    # Show service status first
    if [[ "$VERBOSE" == true ]]; then
        print_header "Service Status"
        docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" ps
        echo ""
    fi
    
    print_header "Logs"
    $LOGS_CMD
else
    # Validate service names
    for SERVICE_NAME in "${SERVICE_NAMES[@]}"; do
        if ! docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" config --services | grep -q "^${SERVICE_NAME}$"; then
            print_error "Service '$SERVICE_NAME' not found in compose file: $COMPOSE_FILE"
            
            print_status "Available services:"
            docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" config --services | sort
            exit 1
        fi
    done
    
    print_status "Showing logs for services: ${SERVICE_NAMES[*]}"
    
    # Show service status first
    if [[ "$VERBOSE" == true ]]; then
        print_header "Service Status"
        for SERVICE_NAME in "${SERVICE_NAMES[@]}"; do
            echo -e "${CYAN}$SERVICE_NAME:${NC}"
            docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" ps "$SERVICE_NAME" | tail -n +2
        done
        echo ""
    fi
    
    print_header "Logs"
    $LOGS_CMD "${SERVICE_NAMES[@]}"
fi

print_success "Log viewing completed!"
print_status "To follow logs continuously: $0 -f [service-name]"
print_status "To view service status: docker compose -f $COMPOSE_FILE --project-name $PROJECT_NAME ps"