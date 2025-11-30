package com.flightapp.flightservice.util;

import com.flightapp.flightservice.dto.response.FlightSummaryDto;
import com.flightapp.flightservice.entity.Flight;

public class FlightServiceHelpers {
	
	public FlightSummaryDto toFlightSummaryDto(Flight flight) {
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
