package com.flightapp.flightservice.dto.response;

import java.util.List;

public class AirlineInventoryResponse {
	
	private String airLineCode;
	
	private int flightsAdded;
	
	private List<Integer> flightIds;
	
	public String getAirLineCode() {
		return airLineCode;
	}
	public void setAirLineCode(String airLineCode) {
		this.airLineCode = airLineCode;
	}
	public int getFlightsAdded() {
		return flightsAdded;
	}
	public void setFlightsAdded(int flightsAdded) {
		this.flightsAdded = flightsAdded;
	}
	public List<Integer> getFlightIds() {
		return flightIds;
	}
	public void setFlightIds(List<Integer> flightIds) {
		this.flightIds = flightIds;
	}
}
