package com.flightapp.service;

import java.util.List;

import com.flightapp.dto.FlightSearchRequest;
import com.flightapp.dto.FlightSummaryDto;

public interface FlightService {
	
	List<FlightSummaryDto> searchFlights(FlightSearchRequest req);
}
