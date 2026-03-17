#!/bin/bash

# DN Quest - Status Checker Script
# This script checks the status of all DN Quest microservices and infrastructure

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
DETAILED=false
HEALTH_CHECK=false
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
DN Quest - Status Checker

Usage: $0 [OPTIONS]

OPTIONS:
    -f, --file FILE         Docker compose file to use (default: docker-compose.yml)
    -e, --environment ENV   Environment to use (dev|prod|full) (default: dev)
    -p, --project NAME      Project name (default: dn-quest)
    -d, --detailed          Show detailed information about each service
    -h, --health            Perform health checks on services
    -v, --verbose           Verbose output
    -h, --help              Show this help message

EXAMPLES:
    # Check basic status
    $0

    # Check detailed status with health checks
    $0 -d -h

    # Check status for production environment
    $0 -e prod -f docker-compose.prod.yml

    # Check status with verbose output
    $0 -v

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
        -d|--detailed)
            DETAILED=true
            shift
            ;;
        -h|--health)
            HEALTH_CHECK=true
            shift
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        --help)
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

print_header "DN Quest Status Checker"
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

# Check Docker daemon
if ! docker info &> /dev/null; then
    print_error "Docker daemon is not running"
    exit 1
fi

# Get service status
print_header "Service Status"
SERVICES_OUTPUT=$(docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" ps)

echo "$SERVICES_OUTPUT"

# Count services
TOTAL_SERVICES=$(echo "$SERVICES_OUTPUT" | tail -n +2 | wc -l)
RUNNING_SERVICES=$(echo "$SERVICES_OUTPUT" | grep "Up" | wc -l)
UNHEALTHY_SERVICES=$(echo "$SERVICES_OUTPUT" | grep "unhealthy" | wc -l)
FAILED_SERVICES=$(echo "$SERVICES_OUTPUT" | grep -E "(Exit|exited)" | wc -l)

echo ""
print_status "Summary:"
echo "  Total services: $TOTAL_SERVICES"
echo "  Running: $RUNNING_SERVICES"
echo "  Unhealthy: $UNHEALTHY_SERVICES"
echo "  Failed: $FAILED_SERVICES"

# Show detailed information if requested
if [[ "$DETAILED" == true ]]; then
    echo ""
    print_header "Detailed Service Information"
    
    # Get all service names
    SERVICES=$(docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" config --services)
    
    for SERVICE in $SERVICES; do
        echo ""
        print_status "Service: $SERVICE"
        
        # Get container info
        CONTAINER_INFO=$(docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" ps -q "$SERVICE")
        
        if [[ -n "$CONTAINER_INFO" ]]; then
            # Get container details
            CONTAINER_ID=$(echo "$CONTAINER_INFO" | head -n 1)
            
            if [[ -n "$CONTAINER_ID" ]]; then
                echo "  Container ID: $CONTAINER_ID"
                
                # Get image
                IMAGE=$(docker inspect --format='{{.Config.Image}}' "$CONTAINER_ID" 2>/dev/null || echo "N/A")
                echo "  Image: $IMAGE"
                
                # Get ports
                PORTS=$(docker inspect --format='{{range $p, $conf := .NetworkSettings.Ports}}{{if $conf}}{{$p}} -> {{(index $conf 0).HostPort}}{{"\n"}}{{end}}{{end}}' "$CONTAINER_ID" 2>/dev/null | head -n 5)
                if [[ -n "$PORTS" ]]; then
                    echo "  Ports:"
                    echo "$PORTS" | sed 's/^/    /'
                fi
                
                # Get resource usage
                if [[ "$VERBOSE" == true ]]; then
                    STATS=$(docker stats --no-stream --format "table {{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}" "$CONTAINER_ID" 2>/dev/null | tail -n +2)
                    if [[ -n "$STATS" ]]; then
                        echo "  Resource Usage:"
                        echo "    CPU\tMemory\tNetwork I/O\tBlock I/O"
                        echo "    $STATS"
                    fi
                fi
            fi
        else
            echo "  Status: Not running"
        fi
    done
fi

# Perform health checks if requested
if [[ "$HEALTH_CHECK" == true ]]; then
    echo ""
    print_header "Health Checks"
    
    # Define health check URLs for services
    declare -A HEALTH_URLS=(
        ["api-gateway"]="http://localhost:8080/actuator/health"
        ["authentication-service"]="http://localhost:8081/api/auth/actuator/health"
        ["user-management-service"]="http://localhost:8082/api/users/actuator/health"
        ["quest-management-service"]="http://localhost:8083/api/quests/actuator/health"
        ["game-engine-service"]="http://localhost:8084/api/game/actuator/health"
        ["team-management-service"]="http://localhost:8085/api/teams/actuator/health"
        ["notification-service"]="http://localhost:8086/api/notifications/actuator/health"
        ["statistics-service"]="http://localhost:8087/api/statistics/actuator/health"
        ["file-storage-service"]="http://localhost:8088/api/files/actuator/health"
        ["frontend"]="http://localhost:3000"
        ["postgres"]="N/A"
        ["redis"]="N/A"
        ["kafka"]="N/A"
        ["zookeeper"]="N/A"
        ["minio"]="http://localhost:9000/minio/health/live"
        ["nginx"]="http://localhost:80"
    )
    
    # Add monitoring services for full environment
    if [[ "$ENVIRONMENT" == "full" ]]; then
        HEALTH_URLS["prometheus"]="http://localhost:9090/-/healthy"
        HEALTH_URLS["grafana"]="http://localhost:3001/api/health"
        HEALTH_URLS["jaeger"]="http://localhost:16686/"
        HEALTH_URLS["elasticsearch"]="http://localhost:9200/_cluster/health"
        HEALTH_URLS["kibana"]="http://localhost:5601/api/status"
    fi
    
    # Check health for each service
    for SERVICE in $(docker compose -f "$COMPOSE_FILE" --project-name "$PROJECT_NAME" config --services); do
        if [[ -n "${HEALTH_URLS[$SERVICE]}" ]]; then
            URL="${HEALTH_URLS[$SERVICE]}"
            
            if [[ "$URL" == "N/A" ]]; then
                echo "  $SERVICE: Health check not available"
            else
                if curl -sf --max-time 5 "$URL" > /dev/null 2>&1; then
                    echo -e "  $SERVICE: ${GREEN}Healthy${NC}"
                else
                    echo -e "  $SERVICE: ${RED}Unhealthy${NC} ($URL)"
                fi
            fi
        else
            echo "  $SERVICE: No health check defined"
        fi
    done
fi

# Show system information if verbose
if [[ "$VERBOSE" == true ]]; then
    echo ""
    print_header "System Information"
    
    # Docker info
    echo "Docker Version: $(docker --version)"
    echo "Docker Compose Version: $(docker compose --version)"
    
    # Resource usage
    echo ""
    echo "System Resources:"
    echo "  Disk Usage:"
    df -h | grep -E "(Filesystem|/dev/)" | head -5
    
    echo "  Memory Usage:"
    free -h
    
    echo "  CPU Usage:"
    top -bn1 | grep "Cpu(s)" | awk '{print "    " $2}'
    
    # Docker system info
    echo ""
    echo "Docker System:"
    docker system df --format "table {{.Type}}\t{{.TotalCount}}\t{{.Size}}\t{{.Reclaimable}}"
fi

# Show access URLs
echo ""
print_header "Access URLs"

case $ENVIRONMENT in
    dev)
        echo "  Frontend:           http://localhost:3000"
        echo "  API Gateway:        http://localhost:8080"
        echo "  API Documentation:  http://localhost:8080/swagger-ui.html"
        echo "  Kafka UI:           http://localhost:8089"
        echo "  MinIO Console:      http://localhost:9001"
        ;;
    prod)
        echo "  Frontend:           http://localhost:80"
        echo "  API Gateway:        http://localhost:8080"
        echo "  API Documentation:  http://localhost:8080/swagger-ui.html"
        ;;
    full)
        echo "  Frontend:           http://localhost:3000"
        echo "  API Gateway:        http://localhost:8080"
        echo "  API Documentation:  http://localhost:8080/swagger-ui.html"
        echo "  Kafka UI:           http://localhost:8089"
        echo "  MinIO Console:      http://localhost:9001"
        echo "  Prometheus:         http://localhost:9090"
        echo "  Grafana:            http://localhost:3001"
        echo "  Jaeger:             http://localhost:16686"
        echo "  Kibana:             http://localhost:5601"
        ;;
esac

# Show commands
echo ""
print_header "Useful Commands"
echo "  Start services:     ./scripts/start-all.sh -e $ENVIRONMENT"
echo "  Stop services:      ./scripts/stop-all.sh -e $ENVIRONMENT"
echo "  Restart service:    ./scripts/restart-service.sh [service-name]"
echo "  View logs:          ./scripts/logs.sh [service-name]"
echo "  Check status:       ./scripts/status.sh -e $ENVIRONMENT"

print_success "Status check completed!"