#!/bin/bash

# DN Quest - Stop All Services Script
# This script stops all DN Quest microservices and infrastructure

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
REMOVE_VOLUMES=false
REMOVE_IMAGES=false
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
DN Quest - Stop All Services

Usage: $0 [OPTIONS]

OPTIONS:
    -f, --file FILE         Docker compose file to use (default: docker-compose.yml)
    -e, --environment ENV   Environment to use (dev|prod|full) (default: dev)
    -p, --project NAME      Project name (default: dn-quest)
    -v, --volumes           Remove named volumes declared in the `volumes` section
    -i, --images            Remove images used by services
    --force                 Force removal of containers without waiting for graceful shutdown
    -v, --verbose           Verbose output
    -h, --help              Show this help message

EXAMPLES:
    # Stop development environment
    $0

    # Stop full environment and remove volumes
    $0 -e full -f docker-compose.full.yml -v

    # Stop production environment and remove everything
    $0 -e prod -f docker-compose.prod.yml -v -i

    # Stop with custom project name
    $0 -p my-dn-quest

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
        -v|--volumes)
            REMOVE_VOLUMES=true
            shift
            ;;
        -i|--images)
            REMOVE_IMAGES=true
            shift
            ;;
        --force)
            FORCE_STOP=true
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

# Set environment variables
export COMPOSE_PROJECT_NAME="$PROJECT_NAME"
export ENVIRONMENT="$ENVIRONMENT"

print_status "Stopping DN Quest services..."
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

# Check if services are running
RUNNING_SERVICES=$(docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" ps --filter "status=running" --format "table {{.Service}}" | tail -n +2)

if [[ -z "$RUNNING_SERVICES" ]]; then
    print_warning "No services are currently running for project: $PROJECT_NAME"
    exit 0
fi

print_status "Currently running services:"
echo "$RUNNING_SERVICES"

# Build down command
DOWN_CMD="docker compose -f $COMPOSE_FILE --project-name $PROJECT_NAME down"

if [[ "$REMOVE_VOLUMES" == true ]]; then
    DOWN_CMD="$DOWN_CMD --volumes"
fi

if [[ "$REMOVE_IMAGES" == true ]]; then
    DOWN_CMD="$DOWN_CMD --rmi all"
fi

if [[ "$FORCE_STOP" == true ]]; then
    DOWN_CMD="$DOWN_CMD --timeout 0"
fi

# Stop services
print_status "Stopping services..."
if [[ "$VERBOSE" == true ]]; then
    $DOWN_CMD
else
    $DOWN_CMD --quiet
fi

# Clean up orphaned containers if verbose
if [[ "$VERBOSE" == true ]]; then
    print_status "Cleaning up orphaned containers..."
    docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" down --remove-orphans
fi

# Show remaining containers
REMAINING_CONTAINERS=$(docker ps -a --filter "name=$PROJECT_NAME" --format "table {{.Names}}\t{{.Status}}" | tail -n +2)

if [[ -n "$REMAINING_CONTAINERS" ]]; then
    print_warning "Some containers remain:"
    echo "$REMAINING_CONTAINERS"
    
    if [[ "$REMOVE_IMAGES" == true ]]; then
        print_status "Force removing remaining containers..."
        docker ps -a --filter "name=$PROJECT_NAME" -q | xargs -r docker rm -f
    fi
else
    print_success "All containers stopped and removed successfully!"
fi

# Show remaining volumes if volumes were not removed
if [[ "$REMOVE_VOLUMES" == false ]]; then
    REMAINING_VOLUMES=$(docker volume ls --filter "name=$PROJECT_NAME" --format "table {{.Name}}")
    
    if [[ -n "$REMAINING_VOLUMES" ]]; then
        print_status "Remaining volumes (use -v to remove):"
        echo "$REMAINING_VOLUMES"
    fi
fi

# Show remaining images if images were not removed
if [[ "$REMOVE_IMAGES" == false ]]; then
    REMAINING_IMAGES=$(docker images --filter "reference=$PROJECT_NAME*" --format "table {{.Repository}}\t{{.Tag}}")
    
    if [[ -n "$REMAINING_IMAGES" ]]; then
        print_status "Remaining images (use -i to remove):"
        echo "$REMAINING_IMAGES"
    fi
fi

print_success "DN Quest services stopped successfully!"
print_status "To start services again: ./scripts/start-all.sh -e $ENVIRONMENT -f $COMPOSE_FILE"