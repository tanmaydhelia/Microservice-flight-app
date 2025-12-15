package com.flightapp.flightservice.implimentation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	
	public AdminServiceImpl(AirlineRepository airlineRepositroy, FlightRepository flightRepository) {
		this.airlineRepositroy = airlineRepositroy;
		this.flightRepository = flightRepository;
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
