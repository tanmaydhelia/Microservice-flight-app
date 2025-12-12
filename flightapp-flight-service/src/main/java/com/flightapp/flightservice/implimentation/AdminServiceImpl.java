package com.flightapp.flightservice.implimentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightapp.flightservice.dto.request.AirlineInventoryRequest;
import com.flightapp.flightservice.dto.request.FlightInventoryItemDto;
import com.flightapp.flightservice.dto.response.AirlineInventoryResponse;
import com.flightapp.flightservice.entity.Airline;
import com.flightapp.flightservice.entity.Flight;
import com.flightapp.flightservice.entity.FlightStatus;
import com.flightapp.flightservice.exception.ResourceNotFoundException;
import com.flightapp.flightservice.repository.AirlineRepository;
import com.flightapp.flightservice.repository.FlightRepository;
import com.flightapp.flightservice.service.AdminService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService{
	private final AirlineRepository airlineRepositroy;
	private final FlightRepository flightRepository;
	private final ObjectMapper objectMapper;
	
	public AdminServiceImpl(AirlineRepository airlineRepositroy, FlightRepository flightRepository, ObjectMapper objectMapper ) {
		this.airlineRepositroy = airlineRepositroy;
		this.flightRepository = flightRepository;
		this.objectMapper = objectMapper;
	}

	@Override
	@Transactional
	public AirlineInventoryResponse addInventory(AirlineInventoryRequest request) {
		String airlineCode = request.getAirlineCode();
		log.info("Adding inventory for airlineCode={} with {} flights", airlineCode, request.getFlights().size());
		
		Airline airline  = airlineRepositroy.findByCode(airlineCode).orElseThrow(()-> new ResourceNotFoundException("Airline not found with Code: "+airlineCode));
		
		List<Flight> flightsToSave = new ArrayList<>();
		
		for(FlightInventoryItemDto item : request.getFlights()){
			validateFlightInventoryItem(item);
			
			Flight flight = new Flight();
			flight.setAirline(airline);
			flight.setFromAirport(item.getFromAirport());
			flight.setToAirport(item.getToAirport());
			flight.setDepartureTime(item.getDepartureTime());
			flight.setArrivalTime(item.getArrivalTime()	);
			flight.setPrice(item.getPrice());
			flight.setTotalSeats(item.getTotalSeats());
			flight.setAvailableSeats(item.getTotalSeats());	
			flight.setStatus(FlightStatus.SCHEDULED);
			
			flightsToSave.add(flight);
		}
		
		List<Flight> savedFlights = flightRepository.saveAll(flightsToSave);
		
		List<Integer> flightIds = savedFlights.stream().map(flight->flight.getId()).toList();
		
		log.info("Successfully added {} flights for airlineCode {}",savedFlights.size(), airlineCode);
		
		AirlineInventoryResponse res = new AirlineInventoryResponse();
		res.setAirLineCode(airlineCode);
		res.setFlightsAdded(savedFlights.size());
		res.setFlightIds(flightIds);
		
		return res;
	}

	@Override
    @Transactional
    public AirlineInventoryResponse uploadInventoryFile(MultipartFile file, String airlineCode) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        List<FlightInventoryItemDto> flightItems;
        String fileName = file.getOriginalFilename();

        try {
            if (fileName != null && fileName.toLowerCase().endsWith(".json")) {
                flightItems = parseJsonFile(file);
            } else if (fileName != null && fileName.toLowerCase().endsWith(".csv")) {
                flightItems = parseCsvFile(file);
            } else {
                throw new IllegalArgumentException("Unsupported file format. Please upload .json or .csv");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to process file: " + e.getMessage(), e);
        }

        // Create request and reuse existing addInventory logic
        AirlineInventoryRequest request = new AirlineInventoryRequest();
        request.setAirlineCode(airlineCode);
        request.setFlights(flightItems);

        return addInventory(request);
    }

    private List<FlightInventoryItemDto> parseCsvFile(MultipartFile file) throws IOException {
        List<FlightInventoryItemDto> flightItems = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm a");

        // Open the file stream
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader, 
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (CSVRecord csvRecord : csvParser) {
                // Ensure the row has enough data to avoid IndexOutOfBounds
                if (csvRecord.size() < 6) continue; 

                FlightInventoryItemDto item = new FlightInventoryItemDto();
                
                // You can access by column name if your CSV has headers, or by index
                // Example CSV Header: From,To,Departure,Arrival,Price,Seats
                
                item.setFromAirport(csvRecord.get("From"));
                item.setToAirport(csvRecord.get("To"));
                
                item.setDepartureTime(LocalDateTime.parse(csvRecord.get("Departure"), formatter));
                item.setArrivalTime(LocalDateTime.parse(csvRecord.get("Arrival"), formatter));
                
                item.setPrice(Integer.parseInt(csvRecord.get("Price")));
                item.setTotalSeats(Integer.parseInt(csvRecord.get("Seats")));
                item.setAvailabeSeats(item.getTotalSeats());

                flightItems.add(item);
            }
        }
        return flightItems;
    }
    
    private List<FlightInventoryItemDto> parseJsonFile(MultipartFile file) throws IOException {
        // Reads a JSON array of FlightInventoryItemDto
        return objectMapper.readValue(file.getInputStream(), new TypeReference<List<FlightInventoryItemDto>>() {});
    }
    
	private void validateFlightInventoryItem(FlightInventoryItemDto item) {
		LocalDateTime dep = item.getDepartureTime();
		LocalDateTime arr = item.getArrivalTime();
		
		if(dep==null || arr==null) {
			throw new IllegalArgumentException("Equal Arrival and Dept Time...");
		}
		if (!arr.isAfter(dep)) {
            throw new IllegalArgumentException("Arrival must be after departure...");
        }
        if (item.getTotalSeats() <= 0) {
            throw new IllegalArgumentException("Total Seats is less than zero...");
        }
	}
	
	
	
	
}
