#!/bin/bash

# DN Quest Microservices Start Script
# This script starts all microservices using Docker Compose

set -e

echo "🚀 Starting DN Quest Microservices..."

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

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed. Please install Docker Compose and try again."
    exit 1
fi

# Check if docker-compose.yml exists
if [ ! -f "docker-compose.yml" ]; then
    print_error "docker-compose.yml not found. Please ensure you're in the project root directory."
    exit 1
fi

# Parse command line arguments
ENVIRONMENT="dev"
BUILD_FIRST=false
STOP_FIRST=false
SERVICES=()

while [[ $# -gt 0 ]]; do
    case $1 in
        --env)
            ENVIRONMENT="$2"
            shift 2
            ;;
        --build)
            BUILD_FIRST=true
            shift
            ;;
        --stop)
            STOP_FIRST=true
            shift
            ;;
        --services)
            shift
            while [[ $# -gt 0 && ! $1 =~ ^-- ]]; do
                SERVICES+=("$1")
                shift
            done
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --env ENVIRONMENT    Set environment (dev, prod, test) [default: dev]"
            echo "  --build              Build images before starting"
            echo "  --stop               Stop existing containers before starting"
            echo "  --services SERVICE1,SERVICE2,...  Start only specific services"
            echo "  --help, -h           Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                           # Start all services in dev mode"
            echo "  $0 --env prod                # Start all services in prod mode"
            echo "  $0 --build                   # Build and start all services"
            echo "  $0 --services api-gateway,auth  # Start only specific services"
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            echo "Use --help for usage information."
            exit 1
            ;;
    esac
done

# Stop existing containers if requested
if [ "$STOP_FIRST" = true ]; then
    print_status "Stopping existing containers..."
    docker-compose down
    print_success "Existing containers stopped"
fi

# Build images if requested
if [ "$BUILD_FIRST" = true ]; then
    print_status "Building Docker images..."
    ./scripts/build.sh
    print_success "Docker images built"
fi

# Set environment variables
export COMPOSE_PROJECT_NAME="dn-quest"
export ENVIRONMENT="$ENVIRONMENT"

print_status "Starting DN Quest microservices in $ENVIRONMENT environment..."

# Start infrastructure services first
print_status "Starting infrastructure services (PostgreSQL, Redis, Kafka, MinIO)..."
docker-compose up -d postgres-auth postgres-users postgres-quests postgres-game postgres-teams postgres-notifications postgres-statistics postgres-files redis zookeeper kafka kafka-ui minio

# Wait for infrastructure services to be ready
print_status "Waiting for infrastructure services to be ready..."
sleep 30

# Check if infrastructure services are healthy
print_status "Checking infrastructure service health..."

# Check PostgreSQL databases
for db in postgres-auth postgres-users postgres-quests postgres-game postgres-teams postgres-notifications postgres-statistics postgres-files; do
    if docker-compose ps $db | grep -q "Up (healthy)"; then
        print_success "$db is healthy"
    else
        print_warning "$db is not healthy yet, waiting..."
        sleep 10
    fi
done

# Check Redis
if docker-compose ps redis | grep -q "Up (healthy)"; then
    print_success "Redis is healthy"
else
    print_warning "Redis is not healthy yet, waiting..."
    sleep 10
fi

# Check Kafka
if docker-compose ps kafka | grep -q "Up (healthy)"; then
    print_success "Kafka is healthy"
else
    print_warning "Kafka is not healthy yet, waiting..."
    sleep 10
fi

# Start microservices
if [ ${#SERVICES[@]} -eq 0 ]; then
    print_status "Starting all microservices..."
    docker-compose up -d api-gateway authentication-service user-management-service quest-management-service game-engine-service team-management-service notification-service statistics-service file-storage-service
else
    print_status "Starting specific services: ${SERVICES[*]}"
    docker-compose up -d "${SERVICES[@]}"
fi

# Wait for microservices to start
print_status "Waiting for microservices to start..."
sleep 30

# Check service health
print_status "Checking microservice health..."

services_to_check=(
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

for service in "${services_to_check[@]}"; do
    service_name=$(echo $service | cut -d':' -f1)
    port=$(echo $service | cut -d':' -f2)
    
    if [ ${#SERVICES[@]} -eq 0 ] || [[ " ${SERVICES[*]} " =~ " ${service_name} " ]]; then
        if curl -f -s http://localhost:$port/actuator/health > /dev/null 2>&1; then
            print_success "$service_name is healthy"
        else
            print_warning "$service_name is not responding yet"
        fi
    fi
done

print_success "🎉 DN Quest microservices started successfully!"
echo ""
print_status "Service URLs:"
echo "  🌐 API Gateway: http://localhost:8080"
echo "  🔐 Authentication Service: http://localhost:8081"
echo "  👥 User Management Service: http://localhost:8082"
echo "  📋 Quest Management Service: http://localhost:8083"
echo "  🎮 Game Engine Service: http://localhost:8084"
echo "  👨‍👩‍👧‍👦 Team Management Service: http://localhost:8085"
echo "  🔔 Notification Service: http://localhost:8086"
echo "  📊 Statistics Service: http://localhost:8087"
echo "  📁 File Storage Service: http://localhost:8088"
echo ""
print_status "Infrastructure URLs:"
echo "  🗄️  Kafka UI: http://localhost:8089"
echo "  🪣 MinIO Console: http://localhost:9001 (minioadmin/minioadmin)"
echo ""
print_status "Useful Commands:"
echo "  📋 View logs: docker-compose logs -f [service-name]"
echo "  🛑 Stop services: docker-compose down"
echo "  🔄 Restart services: docker-compose restart [service-name]"
echo "  📊 Check status: docker-compose ps"
echo ""
print_status "API Documentation:"
echo "  📚 Swagger UI: http://localhost:8080/swagger-ui.html"
echo "  📚 API Docs: http://localhost:8080/api-docs"