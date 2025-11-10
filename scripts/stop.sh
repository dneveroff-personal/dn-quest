#!/bin/bash

# DN Quest Microservices Stop Script
# This script stops all microservices and cleans up resources

set -e

echo "🛑 Stopping DN Quest Microservices..."

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
    print_error "Docker is not running."
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed."
    exit 1
fi

# Check if docker-compose.yml exists
if [ ! -f "docker-compose.yml" ]; then
    print_error "docker-compose.yml not found. Please ensure you're in the project root directory."
    exit 1
fi

# Parse command line arguments
REMOVE_VOLUMES=false
REMOVE_IMAGES=false
FORCE=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --volumes)
            REMOVE_VOLUMES=true
            shift
            ;;
        --images)
            REMOVE_IMAGES=true
            shift
            ;;
        --force|-f)
            FORCE=true
            shift
            ;;
        --all)
            REMOVE_VOLUMES=true
            REMOVE_IMAGES=true
            FORCE=true
            shift
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --volumes           Remove all volumes (WARNING: This will delete all data!)"
            echo "  --images            Remove all DN Quest Docker images"
            echo "  --force, -f         Force removal without confirmation"
            echo "  --all               Remove everything (volumes, images, force)"
            echo "  --help, -h          Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                  # Stop all services"
            echo "  $0 --volumes        # Stop services and remove volumes"
            echo "  $0 --all            # Stop services and remove everything"
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            echo "Use --help for usage information."
            exit 1
            ;;
    esac
done

# Confirmation for destructive operations
if [ "$REMOVE_VOLUMES" = true ] || [ "$REMOVE_IMAGES" = true ]; then
    if [ "$FORCE" = false ]; then
        echo ""
        print_warning "⚠️  WARNING: This operation will delete data!"
        if [ "$REMOVE_VOLUMES" = true ]; then
            echo "  - All database volumes will be removed"
            echo "  - All Redis data will be removed"
            echo "  - All MinIO data will be removed"
        fi
        if [ "$REMOVE_IMAGES" = true ]; then
            echo "  - All DN Quest Docker images will be removed"
        fi
        echo ""
        read -p "Are you sure you want to continue? (y/N): " -n 1 -r
        echo ""
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_status "Operation cancelled."
            exit 0
        fi
    fi
fi

# Stop all services
print_status "Stopping all DN Quest services..."
docker-compose down

if [ $? -eq 0 ]; then
    print_success "All services stopped successfully"
else
    print_warning "Some services may still be running"
fi

# Remove volumes if requested
if [ "$REMOVE_VOLUMES" = true ]; then
    print_status "Removing all volumes..."
    docker-compose down -v
    
    # Remove orphaned volumes
    print_status "Removing orphaned volumes..."
    docker volume prune -f
    
    print_success "All volumes removed"
fi

# Remove images if requested
if [ "$REMOVE_IMAGES" = true ]; then
    print_status "Removing DN Quest Docker images..."
    
    # Remove all DN Quest images
    docker images dn-quest/* --format "table {{.Repository}}:{{.Tag}}" | grep -v REPOSITORY | while read -r image; do
        if [ ! -z "$image" ]; then
            print_status "Removing image: $image"
            docker rmi "$image" 2>/dev/null || true
        fi
    done
    
    # Remove dangling images
    print_status "Removing dangling images..."
    docker image prune -f
    
    print_success "All DN Quest images removed"
fi

# Clean up unused networks
print_status "Cleaning up unused networks..."
docker network prune -f

# Show final status
echo ""
print_status "Final Status:"

# Check if any containers are still running
running_containers=$(docker ps --filter "name=dn-quest" --format "table {{.Names}}" | grep -v NAMES | wc -l)
if [ "$running_containers" -gt 0 ]; then
    print_warning "$running_containers DN Quest containers are still running"
    docker ps --filter "name=dn-quest" --format "table {{.Names}}\t{{.Status}}"
else
    print_success "All DN Quest containers are stopped"
fi

# Check if any volumes still exist
if [ "$REMOVE_VOLUMES" = false ]; then
    volumes=$(docker volume ls --filter "name=dn-quest" --format "table {{.Name}}" | grep -v VOLUME | wc -l)
    if [ "$volumes" -gt 0 ]; then
        print_status "$volumes DN Quest volumes still exist"
        echo "  Use --volumes to remove them"
    else
        print_status "No DN Quest volumes found"
    fi
fi

# Check if any images still exist
if [ "$REMOVE_IMAGES" = false ]; then
    images=$(docker images dn-quest/* --format "table {{.Repository}}" | grep -v REPOSITORY | wc -l)
    if [ "$images" -gt 0 ]; then
        print_status "$images DN Quest images still exist"
        echo "  Use --images to remove them"
    else
        print_status "No DN Quest images found"
    fi
fi

echo ""
print_success "🎉 DN Quest microservices stopped successfully!"

if [ "$REMOVE_VOLUMES" = false ] || [ "$REMOVE_IMAGES" = false ]; then
    echo ""
    print_status "To start services again, run: ./scripts/start.sh"
    echo "To completely clean up, run: ./scripts/stop.sh --all"
fi