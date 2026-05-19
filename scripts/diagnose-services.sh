#!/bin/bash
# DN-Quest Service Diagnostics Script
# Инструмент для диагностики проблем с микросервисами

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
JAEGER_URL="http://localhost:16686"
API_GATEWAY_URL="http://localhost:8080"
AUTH_SERVICE_URL="http://localhost:8081"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  DN-Quest Service Diagnostics${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to check service health
check_service_health() {
    local service_name=$1
    local url=$2
    local endpoint=$3
    
    echo -n "Checking $service_name... "
    
    if curl -s -f "${url}${endpoint}" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ OK${NC}"
        return 0
    else
        echo -e "${RED}✗ FAILED${NC}"
        return 1
    fi
}

# Function to check Jaeger connectivity
check_jaeger() {
    echo -e "\n${YELLOW}=== Jaeger Tracing Check ===${NC}"
    
    # Check if Jaeger UI is accessible
    if curl -s -f "http://localhost:16686" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Jaeger UI accessible at http://localhost:16686${NC}"
    else
        echo -e "${RED}✗ Jaeger UI not accessible${NC}"
    fi
    
    # Check OTLP endpoint
    if curl -s -f "http://localhost:4318/v1/traces" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ OTLP endpoint accessible at http://localhost:4318${NC}"
    else
        echo -e "${YELLOW}! OTLP endpoint check (may require POST request)${NC}"
    fi
}

# Function to check authentication flow
check_auth_flow() {
    echo -e "\n${YELLOW}=== Authentication Flow Check ===${NC}"
    
    # Check if auth service is running
    if curl -s -f "http://localhost:8081/actuator/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Authentication Service is running${NC}"
        
        # Check if validate endpoint exists
        response=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8081/auth/validate" 2>/dev/null || echo "000")
        if [ "$response" = "401" ] || [ "$response" = "403" ]; then
            echo -e "${GREEN}✓ Auth validate endpoint exists (returns $response for missing token)${NC}"
        else
            echo -e "${YELLOW}! Auth validate endpoint returned: $response${NC}"
        fi
    else
        echo -e "${RED}✗ Authentication Service is not running${NC}"
    fi
    
    # Check API Gateway
    if curl -s -f "http://localhost:8080/actuator/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ API Gateway is running${NC}"
    else
        echo -e "${RED}✗ API Gateway is not running${NC}"
    fi
}

# Function to check Docker containers
check_docker_containers() {
    echo -e "\n${YELLOW}=== Docker Containers Status ===${NC}"
    
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}✗ Docker not installed${NC}"
        return
    fi
    
    containers=(
        "dn-quest-jaeger-dev"
        "dn-quest-api-gateway-dev"
        "dn-quest-authentication-service-dev"
        "dn-quest-redis-dev"
        "dn-quest-postgres-dev"
    )
    
    for container in "${containers[@]}"; do
        if docker ps --format '{{.Names}}' | grep -q "^${container}$"; then
            status=$(docker inspect --format='{{.State.Status}}' "$container" 2>/dev/null)
            if [ "$status" = "running" ]; then
                echo -e "${GREEN}✓ $container: running${NC}"
            else
                echo -e "${YELLOW}! $container: $status${NC}"
            fi
        else
            echo -e "${RED}✗ $container: not found${NC}"
        fi
    done
}

# Function to check network connectivity
check_network() {
    echo -e "\n${YELLOW}=== Network Connectivity Check ===${NC}"
    
    # Check if services can reach each other
    if docker exec dn-quest-api-gateway-dev ping -c 1 authentication-service-dev > /dev/null 2>&1; then
        echo -e "${GREEN}✓ API Gateway can reach Authentication Service${NC}"
    else
        echo -e "${RED}✗ API Gateway cannot reach Authentication Service${NC}"
    fi
    
    if docker exec dn-quest-api-gateway-dev ping -c 1 jaeger-dev > /dev/null 2>&1; then
        echo -e "${GREEN}✓ API Gateway can reach Jaeger${NC}"
    else
        echo -e "${RED}✗ API Gateway cannot reach Jaeger${NC}"
    fi
}

# Function to check logs for errors
check_logs() {
    echo -e "\n${YELLOW}=== Recent Error Logs ===${NC}"
    
    echo -e "\n${BLUE}API Gateway (last 20 lines):${NC}"
    docker logs --tail 20 dn-quest-api-gateway-dev 2>/dev/null | grep -i "error\|exception\|warn" || echo "No errors found"
    
    echo -e "\n${BLUE}Authentication Service (last 20 lines):${NC}"
    docker logs --tail 20 dn-quest-authentication-service-dev 2>/dev/null | grep -i "error\|exception\|warn" || echo "No errors found"
}

# Function to test authentication endpoint
test_auth_endpoint() {
    echo -e "\n${YELLOW}=== Testing Authentication Endpoint ===${NC}"
    
     # Test login endpoint
     echo "Testing /api/auth/login endpoint..."
     response=$(curl -s -o /dev/null -w "%{http_code}" -X POST "http://localhost:8080/api/auth/login" \
         -H "Content-Type: application/json" \
         -d '{"username":"admin","password":"admin"}' 2>/dev/null || echo "000")
    
    case $response in
        200) echo -e "${GREEN}✓ Login endpoint working (200)${NC}" ;;
        401) echo -e "${YELLOW}! Login endpoint returned 401 (expected for invalid credentials)${NC}" ;;
        404) echo -e "${RED}✗ Login endpoint not found (404)${NC}" ;;
        000) echo -e "${RED}✗ Cannot connect to login endpoint${NC}" ;;
        *) echo -e "${YELLOW}! Login endpoint returned: $response${NC}" ;;
    esac
}

# Main execution
main() {
    check_docker_containers
    check_jaeger
    check_auth_flow
    check_network
    test_auth_endpoint
    check_logs
    
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}  Diagnostics Complete${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
    echo "For detailed Jaeger traces, visit: $JAEGER_URL"
    echo ""
}

# Run main function
main "$@"