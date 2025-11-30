package com.flightapp.flightservice.implimentation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.flightapp.flightservice.dto.request.FlightSearchRequest;
import com.flightapp.flightservice.dto.response.FlightSummaryDto;
import com.flightapp.flightservice.entity.Flight;
import com.flightapp.flightservice.entity.FlightStatus;
import com.flightapp.flightservice.exception.ResourceNotFoundException;
import com.flightapp.flightservice.exception.SeatNotAvailableException;
import com.flightapp.flightservice.repository.FlightRepository;
import com.flightapp.flightservice.service.FlightService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FlightServiceImpl implements FlightService{
	private final FlightRepository flightRepository;

	public FlightServiceImpl(FlightRepository flightRepository) {
		this.flightRepository = flightRepository;
	}

	@Override
	public List<FlightSummaryDto> searchFlights(FlightSearchRequest req) {
		
		log.info("Searching flights from={} to={} date={} tripType={}", req.getFrom(), req.getTo(), req.getJourneyDate(), req.getTripType());
		
		LocalDate JourneyDate = req.getJourneyDate();
		LocalDateTime start = JourneyDate.atStartOfDay();
		LocalDateTime end = JourneyDate.plusDays(1).atStartOfDay();
		
		List<Flight> flights = flightRepository.findByFromAirportAndToAirportAndDepartureTimeBetweenAndStatus(req.getFrom(), req.getTo(), start, end, FlightStatus.SCHEDULED);
		
		log.debug("Found {} flights", flights.size());
		
		List<FlightSummaryDto> flightDtoList = new ArrayList<>();
		
		for(Flight flight: flights) {
			FlightSummaryDto i = toFlightSummaryDto(flight);
			flightDtoList.add(i);
		}
		
		return flightDtoList;
	}
	
	@Override
	public FlightSummaryDto getFlightById(int flightId) {
	    Flight flight = flightRepository.findById(flightId)
	        .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + flightId));
	    return toFlightSummaryDto(flight);
	}

	@Override
	@Transactional
	public void updateSeats(int flightId, int count) {
	    Flight flight = flightRepository.findById(flightId)
	        .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));
	    
	    int newCount = flight.getAvailableSeats() + count;

	    if(newCount < 0) {
	        throw new SeatNotAvailableException("Not enough seats available");
	    }
	    if(newCount > flight.getTotalSeats()) {
	        newCount = flight.getTotalSeats(); // Cap at total seats
	    }
	    
	    flight.setAvailableSeats(newCount);
	    flightRepository.save(flight);
	}
	
	private FlightSummaryDto toFlightSummaryDto(Flight flight) {
		FlightSummaryDto f = new FlightSummaryDto();
		
		f.setFlightId(flight.getId());
		f.setAirlineName(flight.getAirline().getName());
		f.setAirlineCode(flight.getAirline().getCode());
		f.setFromAirport(flight.getFromAirport());
		f.setToAirport(flight.getToAirport());
		f.setDepartureTime(flight.getDepartureTime());
		f.setArrivalTime(flight.getArrivalTime());
		f.setPrice(flight.getPrice());
		f.setAvailableSeats(flight.getAvailableSeats());
		
		return f;
	}
}
