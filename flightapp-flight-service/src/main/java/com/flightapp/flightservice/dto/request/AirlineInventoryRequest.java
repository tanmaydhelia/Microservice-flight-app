package com.flightapp.flightservice.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public class AirlineInventoryRequest {
	
	@NotBlank
	private String airlineCode;
	
	@NotEmpty
	private List<FlightInventoryItemDto> flights;

	public String getAirlineCode() {
		return airlineCode;
	}

	public void setAirlineCode(String airlineCode) {
		this.airlineCode = airlineCode;
	}

	public List<FlightInventoryItemDto> getFlights() {
		return flights;
	}

	public void setFlights(List<FlightInventoryItemDto> flights) {
		this.flights = flights;
	}
}
