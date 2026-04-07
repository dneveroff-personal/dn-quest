#!/bin/bash

# DN Quest - Local Development Runner
# This script runs all services locally using Docker Compose

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# Configuration
COMPOSE_FILE=${1:-"docker-compose.yml"}
PROFILE=${2:-"dev"}
BUILD_IMAGES=${3:-"true"}

print_status "Starting DN Quest local development environment..."
print_status "Compose file: $COMPOSE_FILE"
print_status "Profile: $PROFILE"
print_status "Build images: $BUILD_IMAGES"

# Check dependencies
check_dependencies() {
    print_status "Checking dependencies..."
    
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed or not in PATH"
        exit 1
    fi
    
    if ! command -v docker compose &> /dev/null && ! docker compose version &> /dev/null; then
        print_error "Docker Compose is not installed or not in PATH"
        exit 1
    fi
    
    print_success "All dependencies are available"
}

# Setup environment
setup_environment() {
    print_status "Setting up environment..."
    
    # Create .env file if it doesn't exist
    if [ ! -f ".env" ]; then
        if [ -f ".env.example" ]; then
            print_status "Creating .env from .env.example..."
            cp .env.example .env
            print_warning "Please review and update .env file with your configuration"
        else
            print_warning "No .env.example file found, using default configuration"
        fi
    fi
    
    # Create necessary directories
    mkdir -p logs
    mkdir -p data/postgres
    mkdir -p data/redis
    mkdir -p data/minio
    
    print_success "Environment setup completed"
}

# Build images if requested
build_images() {
    if [ "$BUILD_IMAGES" = "true" ]; then
        print_status "Building Docker images..."
        
        # Use the build script if available
        if [ -f "scripts/build-all-services.sh" ]; then
            chmod +x scripts/build-all-services.sh
            ./scripts/build-all-services.sh docker dn-quest 1.0.0
        else
            print_warning "Build script not found, building with compose..."
            if docker compose version &> /dev/null; then
                docker compose -f "$COMPOSE_FILE" build
            else
                docker compose -f "$COMPOSE_FILE" build
            fi
        fi
        
        print_success "Images built successfully"
    fi
}

# Start services
start_services() {
    print_status "Starting services..."
    
    # Set environment variables
    export SPRING_PROFILES_ACTIVE="$PROFILE"
    export COMPOSE_PROJECT_NAME="dn-quest"
    
    # Start services
    if docker compose version &> /dev/null; then
        docker compose -f "$COMPOSE_FILE" up -d
    else
        docker compose -f "$COMPOSE_FILE" up -d
    fi
    
    print_success "Services started successfully"
}

# Wait for services to be ready
wait_for_services() {
    print_status "Waiting for services to be ready..."
    
    local services=(
        "postgres-auth:5432"
        "postgres-users:5432"
        "postgres-quests:5432"
        "postgres-game:5432"
        "postgres-teams:5432"
        "postgres-notifications:5432"
        "postgres-statistics:5432"
        "postgres-files:5432"
        "redis:6379"
        "kafka:9092"
        "api-gateway:8080"
        "authentication-service:8081"
        "user-management-service:8082"
        "quest-management-service:8083"
        "game-engine-service:8084"
        "team-management-service:8085"
        "notification-service:8086"
        "statistics-service:8087"
        "file-storage-service:8088"
    )
    
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        local all_ready=true
        
        for service in "${services[@]}"; do
            local service_name=$(echo "$service" | cut -d':' -f1)
            local service_port=$(echo "$service" | cut -d':' -f2)
            
            if ! nc -z localhost "$service_port" 2>/dev/null; then
                all_ready=false
                break
            fi
        done
        
        if [ "$all_ready" = "true" ]; then
            print_success "All services are ready!"
            break
        fi
        
        print_status "Waiting for services... (attempt $attempt/$max_attempts)"
        sleep 10
        ((attempt++))
    done
    
    if [ $attempt -gt $max_attempts ]; then
        print_warning "Some services may not be fully ready yet"
    fi
}

# Show service status
show_status() {
    print_status "Service status:"
    
    if docker compose version &> /dev/null; then
        docker compose -f "$COMPOSE_FILE" ps
    else
        docker compose -f "$COMPOSE_FILE" ps
    fi
    
    echo
    print_status "Service URLs:"
    echo "  API Gateway:           http://localhost:8080"
    echo "  Authentication:        http://localhost:8081"
    echo "  User Management:       http://localhost:8082"
    echo "  Quest Management:      http://localhost:8083"
    echo "  Game Engine:           http://localhost:8084"
    echo "  Team Management:       http://localhost:8085"
    echo "  Notification:          http://localhost:8086"
    echo "  Statistics:            http://localhost:8087"
    echo "  File Storage:          http://localhost:8088"
    echo "  Kafka UI:              http://localhost:8089"
    echo "  MinIO Console:         http://localhost:9001"
    echo
    print_status "Database URLs:"
    echo "  Auth DB:               localhost:5432"
    echo "  Users DB:              localhost:5433"
    echo "  Quests DB:             localhost:5434"
    echo "  Game DB:               localhost:5435"
    echo "  Teams DB:              localhost:5436"
    echo "  Notifications DB:      localhost:5437"
    echo "  Statistics DB:         localhost:5438"
    echo "  Files DB:              localhost:5439"
    echo
    print_status "Other Services:"
    echo "  Redis:                 localhost:6379"
    echo "  Kafka:                 localhost:9092"
    echo "  MinIO:                 localhost:9000"
}

# Cleanup function
cleanup() {
    print_status "Cleaning up..."
    
    if docker compose version &> /dev/null; then
        docker compose -f "$COMPOSE_FILE" down
    else
        docker compose -f "$COMPOSE_FILE" down
    fi
    
    print_success "Cleanup completed"
}

# Main function
main() {
    check_dependencies
    setup_environment
    
    # Handle cleanup on exit
    trap cleanup EXIT
    
    build_images
    start_services
    wait_for_services
    show_status
    
    print_success "DN Quest local environment is ready!"
    print_status "Press Ctrl+C to stop all services"
    
    # Keep script running
    while true; do
        sleep 5
    done
}

# Help function
show_help() {
    echo "Usage: $0 [compose_file] [profile] [build_images]"
    echo ""
    echo "Arguments:"
    echo "  compose_file  Docker Compose file (default: docker-compose.yml)"
    echo "  profile       Spring profile (default: dev)"
    echo "  build_images  Build images before starting (default: true)"
    echo ""
    echo "Examples:"
    echo "  $0                           # Use defaults"
    echo "  $0 docker-compose.dev.yml dev"
    echo "  $0 docker-compose.prod.yml prod false"
    echo ""
    echo "Environment variables:"
    echo "  SPRING_PROFILES_ACTIVE  Spring profile to use"
    echo "  COMPOSE_PROJECT_NAME    Docker Compose project name"
}

# Parse arguments
if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
    show_help
    exit 0
fi

# Handle signals
trap 'print_status "Received signal, stopping services..."; cleanup; exit 0' SIGINT SIGTERM

# Run main function
main "$@"