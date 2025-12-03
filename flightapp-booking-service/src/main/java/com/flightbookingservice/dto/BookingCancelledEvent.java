package com.flightbookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingCancelledEvent {
	private String pnr;
	private Integer flightId;
	private Integer seatsToRelease;
}
