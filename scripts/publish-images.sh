#!/bin/bash

# DN Quest - Publish Images Script
# This script publishes Docker images to a registry

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

REGISTRY=${1:-"dn-quest"}
VERSION=${2:-"1.0.0"}
PUSH_LATEST=${3:-"true"}

print_status "Starting publish process for DN Quest images..."
print_status "Registry: $REGISTRY"
print_status "Version: $VERSION"
print_status "Push latest: $PUSH_LATEST"

# Check if Docker is available
check_dependencies() {
    print_status "Checking dependencies..."
    
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed or not in PATH"
        exit 1
    fi
    
    print_success "Docker is available"
}

# Login to registry
login_to_registry() {
    if [ -n "$DOCKER_REGISTRY_URL" ] && [ -n "$DOCKER_REGISTRY_USERNAME" ] && [ -n "$DOCKER_REGISTRY_PASSWORD" ]; then
        print_status "Logging into registry $DOCKER_REGISTRY_URL..."
        echo "$DOCKER_REGISTRY_PASSWORD" | docker login $DOCKER_REGISTRY_URL -u "$DOCKER_REGISTRY_USERNAME" --password-stdin || {
            print_error "Failed to login to registry"
            exit 1
        }
        print_success "Successfully logged into registry"
    else
        print_warning "No registry credentials provided, skipping login"
    fi
}

# Publish image
publish_image() {
    local service=$1
    local image_name="${REGISTRY}/${service}:${VERSION}"
    local latest_name="${REGISTRY}/${service}:latest"
    
    print_status "Publishing $service..."
    
    # Check if image exists locally
    if ! docker image inspect "$image_name" &> /dev/null; then
        print_error "Image $image_name not found locally. Build it first."
        return 1
    fi
    
    # Tag for registry if needed
    if [ "$REGISTRY" != "dn-quest" ]; then
        docker tag "dn-quest/${service}:${VERSION}" "$image_name"
        if [ "$PUSH_LATEST" = "true" ]; then
            docker tag "dn-quest/${service}:latest" "$latest_name"
        fi
    fi
    
    # Push versioned image
    print_status "Pushing $image_name..."
    docker push "$image_name" || {
        print_error "Failed to push $image_name"
        return 1
    }
    
    # Push latest if requested
    if [ "$PUSH_LATEST" = "true" ]; then
        print_status "Pushing $latest_name..."
        docker push "$latest_name" || {
            print_error "Failed to push $latest_name"
            return 1
        }
    fi
    
    print_success "Successfully published $service"
}

# Main publish process
main() {
    check_dependencies
    login_to_registry
    
    local failed_services=()
    local successful_services=()
    
    for service in "${SERVICES[@]}"; do
        print_status "Processing service: $service"
        
        if publish_image "$service"; then
            successful_services+=("$service")
        else
            failed_services+=("$service")
        fi
        
        echo "----------------------------------------"
    done
    
    # Summary
    echo
    print_status "Publish Summary:"
    print_success "Successfully published (${#successful_services[@]}): ${successful_services[*]}"
    
    if [ ${#failed_services[@]} -gt 0 ]; then
        print_error "Failed to publish (${#failed_services[@]}): ${failed_services[*]}"
        exit 1
    fi
    
    print_success "All images published successfully!"
    
    # Show published images
    echo
    print_status "Published images:"
    for service in "${successful_services[@]}"; do
        echo "  - ${REGISTRY}/${service}:${VERSION}"
        if [ "$PUSH_LATEST" = "true" ]; then
            echo "  - ${REGISTRY}/${service}:latest"
        fi
    done
}

# Help function
show_help() {
    echo "Usage: $0 [registry] [version] [push_latest]"
    echo ""
    echo "Arguments:"
    echo "  registry      Docker registry (default: 'dn-quest')"
    echo "  version       Image version (default: '1.0.0')"
    echo "  push_latest   Push latest tag (default: 'true')"
    echo ""
    echo "Environment variables:"
    echo "  DOCKER_REGISTRY_URL      Registry URL for login"
    echo "  DOCKER_REGISTRY_USERNAME Registry username"
    echo "  DOCKER_REGISTRY_PASSWORD Registry password"
    echo ""
    echo "Examples:"
    echo "  $0                                    # Use defaults"
    echo "  $0 my-registry.com/dn-quest 1.2.0"
    echo "  $0 my-registry.com/dn-quest 1.2.0 false"
    echo ""
    echo "With registry login:"
    echo "  export DOCKER_REGISTRY_URL=my-registry.com"
    echo "  export DOCKER_REGISTRY_USERNAME=myuser"
    echo "  export DOCKER_REGISTRY_PASSWORD=mypass"
    echo "  $0 my-registry.com/dn-quest 1.2.0"
}

# Parse arguments
if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
    show_help
    exit 0
fi

# Run main function
main "$@"