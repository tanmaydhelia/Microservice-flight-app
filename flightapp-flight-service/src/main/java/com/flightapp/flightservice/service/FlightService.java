package com.flightapp.flightservice.service;

import java.util.List;

import com.flightapp.flightservice.dto.request.FlightSearchRequest;
import com.flightapp.flightservice.dto.response.FlightSummaryDto;

public interface FlightService {
	
	List<FlightSummaryDto> searchFlights(FlightSearchRequest req);
	
	FlightSummaryDto getFlightById(int flightId);
    
	void updateSeats(int flightId, int seats);
	
	
}
