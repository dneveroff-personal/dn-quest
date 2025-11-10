#!/bin/bash

# DN Quest Microservices Build Script
# This script builds all microservices and creates Docker images

set -e

echo "🚀 Starting DN Quest Microservices Build Process..."

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

# Check if Gradle wrapper exists
if [ ! -f "./gradlew" ]; then
    print_error "Gradle wrapper not found. Please ensure you're in the project root directory."
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

print_status "Cleaning previous builds..."
./gradlew clean

print_status "Building shared library..."
./gradlew :dn-quest-shared:build
print_success "Shared library built successfully"

# List of microservices to build
services=(
    "authentication-service"
    "user-management-service"
    "quest-management-service"
    "game-engine-service"
    "team-management-service"
    "notification-service"
    "statistics-service"
    "file-storage-service"
    "api-gateway"
)

# Build each microservice
for service in "${services[@]}"; do
    print_status "Building $service..."
    
    # Check if service directory exists
    if [ ! -d "$service" ]; then
        print_warning "Service directory $service not found. Skipping..."
        continue
    fi
    
    # Build the service
    ./gradlew :$service:build
    
    if [ $? -eq 0 ]; then
        print_success "$service built successfully"
    else
        print_error "Failed to build $service"
        exit 1
    fi
done

print_status "Building all services completed successfully!"

# Build Docker images
print_status "Building Docker images..."

for service in "${services[@]}"; do
    if [ ! -d "$service" ]; then
        continue
    fi
    
    print_status "Building Docker image for $service..."
    
    # Check if Dockerfile exists
    if [ ! -f "$service/Dockerfile" ]; then
        print_warning "Dockerfile not found for $service. Skipping Docker build..."
        continue
    fi
    
    # Build Docker image
    docker build -t dn-quest/$service:latest ./$service
    
    if [ $? -eq 0 ]; then
        print_success "Docker image for $service built successfully"
    else
        print_error "Failed to build Docker image for $service"
        exit 1
    fi
done

# Build frontend Docker image if it exists
if [ -d "frontend" ] && [ -f "frontend/Dockerfile" ]; then
    print_status "Building frontend Docker image..."
    docker build -t dn-quest/frontend:latest ./frontend
    print_success "Frontend Docker image built successfully"
fi

print_success "🎉 All microservices and Docker images built successfully!"
print_status "You can now start the services using: ./scripts/start.sh"
print_status "Or use Docker Compose: docker-compose up -d"

# Display built images
print_status "Built Docker images:"
docker images | grep dn-quest

echo ""
print_status "Build Summary:"
echo "  - Shared Library: ✅"
for service in "${services[@]}"; do
    if [ -d "$service" ]; then
        echo "  - $service: ✅"
    else
        echo "  - $service: ❌ (directory not found)"
    fi
done
if [ -d "frontend" ]; then
    echo "  - Frontend: ✅"
fi