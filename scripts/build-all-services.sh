#!/bin/bash

# DN Quest - Build All Services Script
# This script builds all microservices using Docker and Jib

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
SERVICES=(
    "api-gateway"
    "authentication-service"
    "file-storage-service"
    "game-engine-service"
    "quest-management-service"
    "team-management-service"
    "user-management-service"
    "notification-service"
    "statistics-service"
)

BUILD_TYPE=${1:-docker}
REGISTRY=${2:-"dn-quest"}
VERSION=${3:-"1.0.0"}

print_status "Starting build process for all DN Quest services..."
print_status "Build type: $BUILD_TYPE"
print_status "Registry: $REGISTRY"
print_status "Version: $VERSION"

# Check if required tools are installed
check_dependencies() {
    print_status "Checking dependencies..."
    
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed or not in PATH"
        exit 1
    fi
    
    if ! command -v gradle &> /dev/null && ! [ -f "./gradlew" ]; then
        print_error "Gradle is not installed and gradlew not found"
        exit 1
    fi
    
    print_success "All dependencies are available"
}

# Build service using Docker
build_with_docker() {
    local service=$1
    print_status "Building $service with Docker..."
    
    if [ ! -d "$service" ]; then
        print_error "Service directory $service not found"
        return 1
    fi
    
    cd "$service"
    
    if [ ! -f "Dockerfile" ]; then
        print_error "Dockerfile not found for $service"
        cd ..
        return 1
    fi
    
    # Build the service
    docker build -t "${REGISTRY}/${service}:${VERSION}" \
                  -t "${REGISTRY}/${service}:latest" \
                  . || {
        print_error "Failed to build $service with Docker"
        cd ..
        return 1
    }
    
    cd ..
    print_success "Successfully built $service with Docker"
}

# Build service using Jib
build_with_jib() {
    local service=$1
    print_status "Building $service with Jib..."
    
    if [ ! -d "$service" ]; then
        print_error "Service directory $service not found"
        return 1
    fi
    
    cd "$service"
    
    # Check if Jib plugin is configured
    if ! grep -q "com.google.cloud.tools.jib" build.gradle.kts 2>/dev/null; then
        print_warning "Jib plugin not found in $service, skipping..."
        cd ..
        return 1
    fi
    
    # Build with Jib
    if [ -f "./gradlew" ]; then
        ./gradlew jib -Djib.to.image="${REGISTRY}/${service}:${VERSION}" \
                     -Djib.to.tags="${VERSION},latest" || {
            print_error "Failed to build $service with Jib"
            cd ..
            return 1
        }
    else
        gradle jib -Djib.to.image="${REGISTRY}/${service}:${VERSION}" \
                  -Djib.to.tags="${VERSION},latest" || {
            print_error "Failed to build $service with Jib"
            cd ..
            return 1
        }
    fi
    
    cd ..
    print_success "Successfully built $service with Jib"
}

# Main build process
main() {
    check_dependencies
    
    local failed_services=()
    local successful_services=()
    
    for service in "${SERVICES[@]}"; do
        print_status "Processing service: $service"
        
        if [ "$BUILD_TYPE" = "jib" ]; then
            if build_with_jib "$service"; then
                successful_services+=("$service")
            else
                failed_services+=("$service")
            fi
        else
            if build_with_docker "$service"; then
                successful_services+=("$service")
            else
                failed_services+=("$service")
            fi
        fi
        
        echo "----------------------------------------"
    done
    
    # Summary
    echo
    print_status "Build Summary:"
    print_success "Successfully built (${#successful_services[@]}): ${successful_services[*]}"
    
    if [ ${#failed_services[@]} -gt 0 ]; then
        print_error "Failed to build (${#failed_services[@]}): ${failed_services[*]}"
        exit 1
    fi
    
    print_success "All services built successfully!"
    
    # Show built images
    echo
    print_status "Built Docker images:"
    if command -v docker &> /dev/null; then
        docker images | grep "$REGISTRY" | head -20
    fi
}

# Help function
show_help() {
    echo "Usage: $0 [build_type] [registry] [version]"
    echo ""
    echo "Arguments:"
    echo "  build_type    Build type: 'docker' (default) or 'jib'"
    echo "  registry      Docker registry (default: 'dn-quest')"
    echo "  version       Image version (default: '1.0.0')"
    echo ""
    echo "Examples:"
    echo "  $0                    # Build with Docker using defaults"
    echo "  $0 docker my-registry 1.2.0"
    echo "  $0 jib                 # Build with Jib using defaults"
}

# Parse arguments
if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
    show_help
    exit 0
fi

# Run main function
main "$@"