package com.flightapp.flightservice.dto.response;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class FlightSummaryDto {

	private int flightId;
	private String airlineName;
	private String airlineCode;
	private String fromAirport;
	private String toAirport;
	
	@DateTimeFormat(pattern = "dd/mm/yy hh:mm a")
	private LocalDateTime departureTime;
	
	@DateTimeFormat(pattern = "dd/mm/yy hh:mm a")
	private LocalDateTime arrivalTime;
	
	private int price;
	
	private int availableSeats;

}
