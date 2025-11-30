package com.flightapp.flightservice.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.flightapp.flightservice.entity.TripType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FlightSearchRequest {
	
	@NotBlank
	private String from;
	
	@NotBlank
	private String to;
	
	@NotNull
	@DateTimeFormat(pattern = "dd/mm/yy hh:mm a")
	private LocalDate journeyDate;
	
	@NotNull
	private TripType tripType;
	
	@DateTimeFormat(pattern = "dd/mm/yy hh:mm a")
	private LocalDate returnDate;

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public LocalDate getJourneyDate() {
		return journeyDate;
	}

	public void setJourneyDate(LocalDate journeyDate) {
		this.journeyDate = journeyDate;
	}

	public TripType getTripType() {
		return tripType;
	}

	public void setTripType(TripType tripType) {
		this.tripType = tripType;
	}

	public LocalDate getReturnDate() {
		return returnDate;
	}

	public void setReturnDate(LocalDate returnDate) {
		this.returnDate = returnDate;
	}
}
