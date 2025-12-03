package com.flightbookingservice.dto;

import lombok.Data;

@Data
public class BookingPlacedEvent {
	private String pnr;
    private String email;
    private String name;
}
