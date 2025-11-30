package com.flightapp.service.implimentation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.flightapp.dto.FlightSearchRequest;
import com.flightapp.dto.FlightSummaryDto;
import com.flightapp.entity.Flight;
import com.flightapp.entity.FlightStatus;
import com.flightapp.repository.FlightRepository;
import com.flightapp.service.FlightService;

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
		return f;
	}
}
