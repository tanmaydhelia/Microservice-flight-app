#!/bin/bash

# Flight App Docker Management Script
# This script helps manage the dockerized microservices

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored messages
print_message() {
    color=$1
    message=$2
    echo -e "${color}${message}${NC}"
}

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_message "$RED" "❌ Error: Docker is not running. Please start Docker and try again."
        exit 1
    fi
    print_message "$GREEN" "✓ Docker is running"
}

# Function to start all services
start_services() {
    print_message "$BLUE" "🚀 Starting all services..."
    docker compose up --build -d
    print_message "$GREEN" "✓ All services started successfully"
    print_message "$YELLOW" "⏳ Services are starting up. This may take 2-3 minutes..."
    print_message "$BLUE" "📊 Monitor status: docker compose ps"
    print_message "$BLUE" "📋 View logs: docker compose logs -f"
}

# Function to stop all services
stop_services() {
    print_message "$BLUE" "🛑 Stopping all services..."
    docker compose down
    print_message "$GREEN" "✓ All services stopped"
}

# Function to restart all services
restart_services() {
    print_message "$BLUE" "🔄 Restarting all services..."
    docker compose restart
    print_message "$GREEN" "✓ All services restarted"
}

# Function to show service status
show_status() {
    print_message "$BLUE" "📊 Service Status:"
    docker compose ps
}

# Function to show logs
show_logs() {
    if [ -z "$1" ]; then
        print_message "$BLUE" "📋 Showing logs for all services (Ctrl+C to exit)..."
        docker compose logs -f
    else
        print_message "$BLUE" "📋 Showing logs for $1 (Ctrl+C to exit)..."
        docker compose logs -f "$1"
    fi
}

# Function to clean up everything
cleanup() {
    print_message "$YELLOW" "⚠️  This will remove all containers, networks, and volumes!"
    read -p "Are you sure? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_message "$BLUE" "🧹 Cleaning up..."
        docker compose down -v --rmi local
        print_message "$GREEN" "✓ Cleanup complete"
    else
        print_message "$YELLOW" "Cleanup cancelled"
    fi
}

# Function to check service health
check_health() {
    print_message "$BLUE" "🏥 Checking service health..."
    
    services=("service-registry:8761" "config-server:8888" "api-gateway:9000" 
              "flight-service:9080" "booking-service:9090" "notification-service:9095")
    
    for service in "${services[@]}"; do
        IFS=':' read -r name port <<< "$service"
        if curl -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            print_message "$GREEN" "✓ $name is healthy"
        else
            print_message "$RED" "✗ $name is not responding"
        fi
    done
}

# Function to open Eureka dashboard
open_eureka() {
    print_message "$BLUE" "🌐 Opening Eureka Dashboard..."
    if command -v xdg-open > /dev/null; then
        xdg-open http://localhost:8761
    elif command -v open > /dev/null; then
        open http://localhost:8761
    else
        print_message "$YELLOW" "Please open http://localhost:8761 in your browser"
    fi
}

# Main menu
show_menu() {
    echo ""
    print_message "$BLUE" "====================================="
    print_message "$BLUE" "  Flight App Docker Manager"
    print_message "$BLUE" "====================================="
    echo "1. Start all services"
    echo "2. Stop all services"
    echo "3. Restart all services"
    echo "4. Show service status"
    echo "5. Show logs (all services)"
    echo "6. Show logs (specific service)"
    echo "7. Check service health"
    echo "8. Open Eureka Dashboard"
    echo "9. Clean up (remove all)"
    echo "0. Exit"
    echo ""
}

# Main script
check_docker

if [ $# -eq 0 ]; then
    # Interactive mode
    while true; do
        show_menu
        read -p "Select an option: " choice
        case $choice in
            1) start_services ;;
            2) stop_services ;;
            3) restart_services ;;
            4) show_status ;;
            5) show_logs ;;
            6) 
                echo "Available services: config-server, service-registry, flight-service, booking-service, notification-service, api-gateway"
                read -p "Enter service name: " service_name
                show_logs "$service_name"
                ;;
            7) check_health ;;
            8) open_eureka ;;
            9) cleanup ;;
            0) 
                print_message "$GREEN" "Goodbye!"
                exit 0
                ;;
            *) print_message "$RED" "Invalid option. Please try again." ;;
        esac
        
        if [ "$choice" != "5" ] && [ "$choice" != "6" ]; then
            read -p "Press Enter to continue..."
        fi
    done
else
    # Command line mode
    case "$1" in
        start) start_services ;;
        stop) stop_services ;;
        restart) restart_services ;;
        status) show_status ;;
        logs) show_logs "$2" ;;
        health) check_health ;;
        eureka) open_eureka ;;
        clean) cleanup ;;
        *)
            echo "Usage: $0 {start|stop|restart|status|logs [service]|health|eureka|clean}"
            echo ""
            echo "Commands:"
            echo "  start    - Start all services"
            echo "  stop     - Stop all services"
            echo "  restart  - Restart all services"
            echo "  status   - Show service status"
            echo "  logs     - Show logs (optionally specify service name)"
            echo "  health   - Check service health"
            echo "  eureka   - Open Eureka dashboard"
            echo "  clean    - Clean up everything"
            echo ""
            echo "Run without arguments for interactive menu"
            exit 1
            ;;
    esac
fi
