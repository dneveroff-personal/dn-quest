#!/bin/bash

# DN Quest - Main Management Script
# This is the main script for managing the DN Quest microservices architecture

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color

# Script information
SCRIPT_NAME="DN Quest Manager"
VERSION="1.0.0"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Default values
ENVIRONMENT="dev"
PROJECT_NAME="dn-quest"
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

print_banner() {
    echo -e "${MAGENTA}"
    cat << "EOF"
 _____ _   _ _   _    _    _   _  ____ _____ ____  
| ____| \ | | | | |  / \  | \ | |/ ___| ____|  _ \ 
|  _| |  \| | |_| | / _ \ |  \| | |   |  _| | | | |
| |___| |\  |  _  |/ ___ \| |\  | |___| |___| |_| |
|_____|_| \_|_| |_/_/   \_\_| \_|\____|_____|____/ 
                                                   
    Microservices Architecture Management Script
EOF
    echo -e "${NC}"
}

# Function to show usage
show_usage() {
    cat << EOF
$SCRIPT_NAME v$VERSION

Usage: $0 COMMAND [OPTIONS]

COMMANDS:
    start               Start all services
    stop                Stop all services
    restart             Restart all services
    status              Show service status
    logs                View service logs
    restart-service     Restart a specific service
    build               Build all services
    clean               Clean up containers, images, and volumes
    init                Initialize the project
    help                Show this help message

OPTIONS:
    -e, --environment ENV   Environment to use (dev|prod|full) [default: dev]
    -p, --project NAME      Project name [default: dn-quest]
    -v, --verbose           Verbose output
    -h, --help              Show this help message

EXAMPLES:
    # Start development environment
    $0 start

    # Start production environment
    $0 start -e prod

    # Start full stack with monitoring
    $0 start -e full

    # View logs for all services
    $0 logs

    # Follow logs for authentication service
    $0 logs authentication-service -f

    # Restart specific service
    $0 restart-service api-gateway

    # Check status with health checks
    $0 status -h

    # Clean up everything
    $0 clean

ENVIRONMENTS:
    dev     - Development environment with hot reload and debug ports
    prod    - Production environment with security and performance optimizations
    full    - Full stack with monitoring, logging and tracing

For more information about specific commands, use:
    $0 COMMAND --help

EOF
}

# Function to execute script
execute_script() {
    local script_name="$1"
    shift
    
    local script_path="$SCRIPT_DIR/scripts/$script_name"
    
    if [[ ! -f "$script_path" ]]; then
        print_error "Script not found: $script_path"
        exit 1
    fi
    
    if [[ "$VERBOSE" == true ]]; then
        print_status "Executing: $script_path $*"
    fi
    
    # Set environment variables
    export ENVIRONMENT="$ENVIRONMENT"
    export PROJECT_NAME="$PROJECT_NAME"
    
    # Execute the script
    "$script_path" "$@"
}

# Function to initialize project
init_project() {
    print_header "Initializing DN Quest Project"
    
    # Check if Docker is installed
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    
    if ! command -v docker compose &> /dev/null; then
        print_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi
    
    # Create necessary directories
    print_status "Creating necessary directories..."
    mkdir -p logs
    mkdir -p data/postgres
    mkdir -p data/redis
    mkdir -p data/kafka
    mkdir -p data/minio
    mkdir -p data/elasticsearch
    mkdir -p data/prometheus
    mkdir -p data/grafana
    mkdir -p data/jaeger
    
    # Create environment files if they don't exist
    if [[ ! -f ".env.development" ]]; then
        print_status "Creating development environment file..."
        cp .env.example .env.development 2>/dev/null || print_warning ".env.example not found, creating basic .env.development"
        if [[ ! -f ".env.development" ]]; then
            cat > .env.development << EOF
# DN Quest Development Environment
ENVIRONMENT=development
COMPOSE_PROJECT_NAME=dn-quest

# Database Configuration
POSTGRES_HOST=postgres
POSTGRES_PORT=5432
POSTGRES_DB=dn_quest_dev
POSTGRES_USER=dn_quest_user
POSTGRES_PASSWORD=dev_password_123

# Redis Configuration
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=dev_redis_password

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# MinIO Configuration
MINIO_HOST=minio
MINIO_PORT=9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin123

# JWT Configuration
JWT_SECRET=dev_jwt_secret_key_very_long_and_secure
JWT_EXPIRATION=86400

# Application Configuration
API_GATEWAY_PORT=8080
AUTH_SERVICE_PORT=8081
USER_SERVICE_PORT=8082
QUEST_SERVICE_PORT=8083
GAME_ENGINE_PORT=8084
TEAM_SERVICE_PORT=8085
NOTIFICATION_SERVICE_PORT=8086
STATISTICS_SERVICE_PORT=8087
FILE_STORAGE_PORT=8088
FRONTEND_PORT=3000
EOF
        fi
    fi
    
    if [[ ! -f ".env.production" ]]; then
        print_status "Creating production environment file..."
        cp .env.example .env.production 2>/dev/null || print_warning ".env.example not found, creating basic .env.production"
        if [[ ! -f ".env.production" ]]; then
            cat > .env.production << EOF
# DN Quest Production Environment
ENVIRONMENT=production
COMPOSE_PROJECT_NAME=dn-quest-prod

# Database Configuration
POSTGRES_HOST=postgres
POSTGRES_PORT=5432
POSTGRES_DB=dn_quest_prod
POSTGRES_USER=dn_quest_user
POSTGRES_PASSWORD=CHANGE_ME_SECURE_PASSWORD

# Redis Configuration
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=CHANGE_ME_REDIS_PASSWORD

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# MinIO Configuration
MINIO_HOST=minio
MINIO_PORT=9000
MINIO_ACCESS_KEY=CHANGE_ME_ACCESS_KEY
MINIO_SECRET_KEY=CHANGE_ME_SECRET_KEY

# JWT Configuration
JWT_SECRET=CHANGE_ME_VERY_LONG_AND_SECURE_JWT_SECRET_KEY
JWT_EXPIRATION=3600

# Application Configuration
API_GATEWAY_PORT=8080
AUTH_SERVICE_PORT=8081
USER_SERVICE_PORT=8082
QUEST_SERVICE_PORT=8083
GAME_ENGINE_PORT=8084
TEAM_SERVICE_PORT=8085
NOTIFICATION_SERVICE_PORT=8086
STATISTICS_SERVICE_PORT=8087
FILE_STORAGE_PORT=8088
FRONTEND_PORT=80
EOF
        fi
    fi
    
    # Set permissions
    chmod +x scripts/*.sh
    
    print_success "Project initialized successfully!"
    print_status "Next steps:"
    echo "  1. Review and update environment files (.env.development, .env.production)"
    echo "  2. Run '$0 start' to start the services"
    echo "  3. Run '$0 status' to check service status"
}

# Function to clean up
clean_project() {
    print_header "Cleaning DN Quest Project"
    
    print_warning "This will remove all containers, images, and volumes for the project."
    read -p "Are you sure you want to continue? (y/N): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_status "Stopping and removing all containers..."
        docker compose -f docker-compose.yml --project-name "$PROJECT_NAME" down -v --remove-orphans 2>/dev/null || true
        docker compose -f docker-compose.prod.yml --project-name "$PROJECT_NAME" down -v --remove-orphans 2>/dev/null || true
        docker compose -f docker-compose.full.yml --project-name "$PROJECT_NAME" down -v --remove-orphans 2>/dev/null || true
        
        print_status "Removing project images..."
        docker images --filter "reference=$PROJECT_NAME*" -q | xargs -r docker rmi -f 2>/dev/null || true
        
        print_status "Removing project volumes..."
        docker volume ls --filter "name=$PROJECT_NAME" -q | xargs -r docker volume rm 2>/dev/null || true
        
        print_status "Removing project networks..."
        docker network ls --filter "name=$PROJECT_NAME" -q | xargs -r docker network rm 2>/dev/null || true
        
        print_success "Cleanup completed!"
    else
        print_status "Cleanup cancelled."
    fi
}

# Parse command line arguments
COMMAND=""
COMMAND_ARGS=()

while [[ $# -gt 0 ]]; do
    case $1 in
        -e|--environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -p|--project)
            PROJECT_NAME="$2"
            shift 2
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        start|stop|restart|status|logs|restart-service|build|clean|init|help)
            if [[ -z "$COMMAND" ]]; then
                COMMAND="$1"
            else
                COMMAND_ARGS+=("$1")
            fi
            shift
            ;;
        *)
            if [[ -n "$COMMAND" ]]; then
                COMMAND_ARGS+=("$1")
            else
                print_error "Unknown command: $1"
                show_usage
                exit 1
            fi
            shift
            ;;
    esac
done

# Show banner
print_banner

# Handle commands
case $COMMAND in
    start)
        print_status "Starting DN Quest services..."
        execute_script "start-all.sh" "${COMMAND_ARGS[@]}"
        ;;
    stop)
        print_status "Stopping DN Quest services..."
        execute_script "stop-all.sh" "${COMMAND_ARGS[@]}"
        ;;
    restart)
        print_status "Restarting DN Quest services..."
        execute_script "stop-all.sh" "${COMMAND_ARGS[@]}"
        sleep 2
        execute_script "start-all.sh" "${COMMAND_ARGS[@]}"
        ;;
    status)
        print_status "Checking DN Quest service status..."
        execute_script "status.sh" "${COMMAND_ARGS[@]}"
        ;;
    logs)
        print_status "Viewing DN Quest service logs..."
        execute_script "logs.sh" "${COMMAND_ARGS[@]}"
        ;;
    restart-service)
        print_status "Restarting DN Quest service..."
        execute_script "restart-service.sh" "${COMMAND_ARGS[@]}"
        ;;
    build)
        print_status "Building DN Quest services..."
        execute_script "start-all.sh" -b "${COMMAND_ARGS[@]}"
        ;;
    clean)
        clean_project
        ;;
    init)
        init_project
        ;;
    help|"")
        show_usage
        ;;
    *)
        print_error "Unknown command: $COMMAND"
        show_usage
        exit 1
        ;;
esac