Microservices Flight Booking SystemTransformed a monolithic Flight Management Application into a distributed microservices architecture using the Spring Cloud ecosystem.Architecture & Goals AchievedMicroservice Decomposition: Decomposed the monolith into two autonomous domains: Flight Service (Inventory & Search) and Booking Service (Transactions & Passenger Management).Database Isolation: Enforced the "Database per Service" pattern, utilizing distinct MySQL databases (flight_service_db and booking_db) to ensure loose coupling.Service Discovery: Deployed Netflix Eureka Server to facilitate dynamic registration and discovery of service instances.API Gateway: Implemented Spring Cloud Gateway on Port 9000 as the single ingress point, handling routing and load balancing for all client requests.Inter-Service Communication: Refactored direct method calls into declarative REST communication using OpenFeign, enabling the Booking Service to interact with the Flight Service over the network.Centralized Configuration: Established a Spring Cloud Config Server to externalize and manage application properties from a central repository.Testing & ReportsCode Coverage: Achieved >90% code coverage for business logic, verified through SonarQube and JaCoCo integration.Automated API Testing: Performed end-to-end integration testing using Newman (Postman CLI). Detailed execution reports capturing dynamic variables (Flight IDs, PNRs) are available in the repository.Performance Testing: Conducted concurrent load testing using JMeter CLI (Non-GUI mode). Comprehensive HTML performance dashboards and logs are available in the repository.Project StructureMicroservices-flight-app/
├── flightapp-api-gateway/         # Entry point and routing (Port 9000)
├── flightapp-booking-service/     # Booking business logic (Port 8082)
├── flightapp-config-server/       # Centralized configuration (Port 8888)
├── flightapp-flight-service/      # Inventory and search logic (Port 8081)
├── flightapp-service-registry/    # Service discovery server (Port 8761)
├── jmeter-reports/                # HTML performance dashboards
└── postman-reports/               # Newman execution logs
Architecture Diagramgraph TD
    %% Actors
    User("User / Postman")

    %% Infrastructure Layer
    subgraph Infrastructure [Infrastructure Layer]
        GW["API Gateway<br/>Port: 9000"]
        Eureka["Service Registry<br/>(Eureka)<br/>Port: 8761"]
        Config["Config Server<br/>Port: 8888"]
    end

    %% Services Layer
    subgraph Services [Domain Services]
        FlightSvc["Flight Service<br/>Port: 8081<br/>(Inventory & Search)"]
        BookingSvc["Booking Service<br/>Port: 8082<br/>(Bookings & History)"]
    end

    %% Persistence Layer
    subgraph Persistence [Persistence Layer]
        FlightDB[("Flight DB<br/>MySQL")]
        BookingDB[("Booking DB<br/>MySQL")]
    end

    %% Relationships
    User -- "Requests" --> GW
    
    %% Config connections
    GW -. "Fetch Config" .-> Config
    FlightSvc -. "Fetch Config" .-> Config
    BookingSvc -. "Fetch Config" .-> Config
    Eureka -. "Fetch Config" .-> Config

    %% Discovery connections
    FlightSvc -. "Register" .-> Eureka
    BookingSvc -. "Register" .-> Eureka
    GW -. "Lookup Routes" .-> Eureka

    %% Routing
    GW -- "/FLIGHT-SERVICE/..." --> FlightSvc
    GW -- "/BOOKING-SERVICE/..." --> BookingSvc

    %% Service to Service Communication
    BookingSvc -- "Feign Client<br/>(Update Seats)" --> FlightSvc

    %% DB Connections
    FlightSvc --> FlightDB
    BookingSvc --> BookingDB

    %% Styling
    style GW fill:#f9f,stroke:#333,stroke-width:2px
    style FlightSvc fill:#bbf,stroke:#333,stroke-width:2px
    style BookingSvc fill:#bbf,stroke:#333,stroke-width:2px
    style Config fill:#ff9,stroke:#333,stroke-width:2px
    style Eureka fill:#ff9,stroke:#333,stroke-width:2px

