# Quick Start Guide - Docker

Get the Flight Booking Microservices up and running in minutes!

## Prerequisites

- Docker Desktop installed and running
- At least 4GB RAM available for Docker
- Ports 8761, 8888, 9000, 9080, 9090, 9092, 9095, 2181, 3307, 3308 available

## 🚀 Quick Start (3 Simple Steps)

### Step 1: Navigate to Project Directory
```bash
cd Microservice-flight-app
```

### Step 2: Start All Services
```bash
docker compose up --build -d
```

### Step 3: Wait for Services to Start (2-3 minutes)
```bash
# Monitor startup progress
docker compose logs -f
```

## ✅ Verify Installation

### Check Running Services
```bash
docker compose ps
```

All services should show "running" status.

### Access Eureka Dashboard
Open your browser: http://localhost:8761

You should see all services registered:
- FLIGHTAPP-FLIGHT-SERVICE
- FLIGHTAPP-FLIGHT-BOOKINGSERVICE
- FLIGHTAPP-API-GATEWAY
- FLIGHTAPP-NOTIFICATION-SERVICE

### Test the API
```bash
# Health check
curl http://localhost:9000/actuator/health

# Get flights (via API Gateway)
curl http://localhost:9000/FLIGHT-SERVICE/flights
```

## 🎯 Using the Management Script

We provide a convenient script to manage Docker services:

```bash
# Interactive menu
./docker-manager.sh

# Or use commands directly
./docker-manager.sh start     # Start all services
./docker-manager.sh stop      # Stop all services
./docker-manager.sh status    # Show status
./docker-manager.sh health    # Check health
./docker-manager.sh logs      # Show all logs
./docker-manager.sh logs flight-service  # Show specific service logs
```

## 🔗 Service URLs

| Service | URL |
|---------|-----|
| API Gateway | http://localhost:9000 |
| Eureka Dashboard | http://localhost:8761 |
| Flight Service | http://localhost:9080 |
| Booking Service | http://localhost:9090 |
| Notification Service | http://localhost:9095 |

## 🧪 Quick Test

Once services are running:

```bash
# Create a booking via API Gateway
curl -X POST http://localhost:9000/BOOKING-SERVICE/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "flightId": 1,
    "passengerName": "John Doe",
    "passengerEmail": "john@example.com",
    "numberOfSeats": 2
  }'
```

## 🛑 Stop Services

```bash
docker compose down
```

## 🔄 Restart Services

```bash
docker compose restart
```

## 🧹 Clean Up Everything

```bash
# Remove containers, networks, and volumes
docker compose down -v

# Remove images as well
docker compose down -v --rmi all
```

## 🐛 Common Issues

### Services not starting?
- Check Docker has enough resources (4GB+ RAM)
- Ensure ports are not in use: `docker compose ps`
- View logs: `docker compose logs service-name`

### Database connection errors?
- Wait longer (MySQL takes time to initialize)
- Check MySQL health: `docker compose logs mysql-flight`

### Can't access Eureka?
- Verify it's running: `docker compose ps service-registry`
- Check logs: `docker compose logs service-registry`

## 📚 Need More Help?

See the complete documentation:
- [README-DOCKER.md](README-DOCKER.md) - Detailed Docker guide
- [README.md](README.md) - Complete project documentation

## 💡 Tips

- **First time?** Initial build takes 5-10 minutes
- **Changed code?** Rebuild with `docker compose up --build service-name`
- **Need logs?** Use `docker compose logs -f service-name`
- **Memory issues?** Allocate more RAM to Docker in Docker Desktop settings

## ⚡ Next Steps

1. Import Postman collection from `Postman Collection Report/`
2. Update base URL to `http://localhost:9000`
3. Start testing the APIs!

---

**Ready to develop?** All services support hot-reload. Just rebuild the specific service after code changes!
