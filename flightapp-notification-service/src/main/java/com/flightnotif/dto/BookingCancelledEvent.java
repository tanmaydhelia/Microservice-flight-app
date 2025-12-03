package com.flightnotif.dto;

import lombok.Data;

@Data
public class BookingCancelledEvent {
	private String pnr;
	private Integer flightId;
    private Integer seatsToRelease;
}
