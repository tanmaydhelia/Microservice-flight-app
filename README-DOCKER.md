# Docker Deployment Guide for Microservices Flight App

This guide provides instructions for deploying the Flight Booking Microservices application using Docker and Docker Compose.

## 📋 Prerequisites

Before you begin, ensure you have the following installed:
- Docker (version 20.10 or higher)
- Docker Compose (version 2.0 or higher)

To verify your installation:
```bash
docker --version
docker-compose --version
```

## 🏗️ Architecture Components

The dockerized application includes the following services:

### Infrastructure Services
1. **Config Server** (Port 8888) - Centralized configuration management
2. **Service Registry** (Port 8761) - Eureka service discovery
3. **API Gateway** (Port 9000) - Single entry point for all services

### Business Services
4. **Flight Service** (Port 9080) - Manages flight inventory and search
5. **Booking Service** (Port 9090) - Handles booking transactions
6. **Notification Service** (Port 9095) - Sends notifications via Kafka

### Data Services
7. **MySQL Flight DB** (Port 3307) - Database for flight service
8. **MySQL Booking DB** (Port 3308) - Database for booking service
9. **Kafka** (Port 9092) - Message broker for async communication
10. **Zookeeper** (Port 2181) - Coordination service for Kafka

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/tanmaydhelia/Microservice-flight-app.git
cd Microservice-flight-app
```

### 2. Build and Start All Services
```bash
docker-compose up --build
```

This command will:
- Build Docker images for all microservices
- Start all containers with proper dependencies
- Set up networking between services
- Initialize databases

### 3. Start Services in Detached Mode
If you want to run services in the background:
```bash
docker-compose up --build -d
```

### 4. Monitor Logs
To view logs from all services:
```bash
docker-compose logs -f
```

To view logs from a specific service:
```bash
docker-compose logs -f service-name
# Examples:
docker-compose logs -f flight-service
docker-compose logs -f booking-service
docker-compose logs -f api-gateway
```

## 🔍 Service Startup Order

The services start in the following order (managed by Docker Compose dependencies):

1. **MySQL databases** and **Zookeeper/Kafka** start first
2. **Config Server** starts next
3. **Service Registry (Eureka)** waits for Config Server
4. **Flight Service**, **Booking Service**, and **Notification Service** wait for their dependencies
5. **API Gateway** starts last, after all other services are healthy

## 🌐 Accessing Services

Once all services are running, you can access:

| Service | URL | Description |
|---------|-----|-------------|
| API Gateway | http://localhost:9000 | Main entry point for API calls |
| Eureka Dashboard | http://localhost:8761 | Service registry dashboard |
| Config Server | http://localhost:8888 | Configuration server |
| Flight Service | http://localhost:9080 | Direct access to flight service |
| Booking Service | http://localhost:9090 | Direct access to booking service |
| Notification Service | http://localhost:9095 | Direct access to notification service |

## 📊 Health Checks

All services expose health check endpoints via Spring Boot Actuator:

```bash
# Check service health
curl http://localhost:8761/actuator/health  # Eureka
curl http://localhost:9000/actuator/health  # API Gateway
curl http://localhost:9080/actuator/health  # Flight Service
curl http://localhost:9090/actuator/health  # Booking Service
curl http://localhost:9095/actuator/health  # Notification Service
```

## 🗄️ Database Access

### Flight Service Database
```bash
docker exec -it mysql-flight mysql -u root -p16434 flight_service_DB
```

### Booking Service Database
```bash
docker exec -it mysql-booking mysql -u root -p16434 booking_DB
```

## 🔧 Common Operations

### Stop All Services
```bash
docker-compose down
```

### Stop and Remove All Data (including volumes)
```bash
docker-compose down -v
```

### Restart a Specific Service
```bash
docker-compose restart service-name
# Example:
docker-compose restart flight-service
```

### Rebuild a Specific Service
```bash
docker-compose up --build service-name -d
# Example:
docker-compose up --build flight-service -d
```

### View Running Containers
```bash
docker-compose ps
```

### Scale a Service (if needed)
```bash
docker-compose up --scale flight-service=2 -d
```

## 🧪 Testing the Application

### 1. Wait for All Services to be Healthy
It may take 2-3 minutes for all services to start and register with Eureka.

Check Eureka dashboard: http://localhost:8761

### 2. Test via API Gateway
All API calls should go through the API Gateway:

```bash
# Example: Get all flights
curl http://localhost:9000/FLIGHT-SERVICE/flights

# Example: Create a booking
curl -X POST http://localhost:9000/BOOKING-SERVICE/bookings \
  -H "Content-Type: application/json" \
  -d '{"flightId": 1, "passengerName": "John Doe"}'
```

### 3. Use Postman Collection
Import the Postman collection from `Postman Collection Report/FlightApp Gateway.postman_collection.json` and update the base URL to `http://localhost:9000`.

## 🐛 Troubleshooting

### Services Not Starting
1. Check logs: `docker-compose logs service-name`
2. Verify ports are not in use: `netstat -an | grep PORT_NUMBER`
3. Ensure Docker has enough resources (CPU, Memory)

### Database Connection Issues
1. Wait for MySQL containers to be fully initialized (check health status)
2. Verify database credentials in docker-compose.yml
3. Check database logs: `docker-compose logs mysql-flight`

### Kafka Connection Issues
1. Ensure Kafka and Zookeeper are running: `docker-compose ps`
2. Check Kafka logs: `docker-compose logs kafka`
3. Verify Kafka is accessible: `docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092`

### Service Discovery Issues
1. Check Eureka dashboard: http://localhost:8761
2. Wait for services to register (can take 30-60 seconds)
3. Verify network connectivity between containers

## 🔒 Security Considerations

For production deployment:
1. Change default database passwords
2. Use environment variables for sensitive data
3. Enable authentication for Eureka and Kafka
4. Use Docker secrets for credentials
5. Implement SSL/TLS for service communication

## 📝 Environment Variables

Key environment variables that can be customized:

### Database Configuration
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

### Service Discovery
- `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`
- `EUREKA_INSTANCE_HOSTNAME`

### Kafka Configuration
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`

### Config Server
- `SPRING_CONFIG_IMPORT`

## 🔄 Updating Services

To update a service after code changes:

```bash
# Rebuild and restart specific service
docker-compose up --build service-name -d

# Example:
docker-compose up --build flight-service -d
```

## 🧹 Cleanup

### Remove All Containers and Networks
```bash
docker-compose down
```

### Remove All Containers, Networks, and Volumes
```bash
docker-compose down -v
```

### Remove All Images
```bash
docker-compose down --rmi all
```

### Complete Cleanup
```bash
docker-compose down -v --rmi all
docker system prune -a
```

## 📚 Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [Main README](README.md)

## 💡 Tips

1. **First Time Setup**: Initial build may take 5-10 minutes as Docker downloads base images and Maven dependencies
2. **Resource Requirements**: Ensure Docker Desktop has at least 4GB RAM and 2 CPUs allocated
3. **Startup Time**: Services start sequentially; allow 2-3 minutes for full system initialization
4. **Log Monitoring**: Use `docker-compose logs -f` during startup to monitor progress
5. **Port Conflicts**: If ports are in use, you can modify them in docker-compose.yml

## 🤝 Contributing

For issues or improvements related to Docker deployment, please open an issue or submit a pull request.
