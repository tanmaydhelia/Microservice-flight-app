package com.flightapp.flightservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.flightservice.dto.response.FlightSummaryDto;
import com.flightapp.flightservice.service.FlightService;

@RestController
@RequestMapping("/api/internal/flight")
public class InternalFlightController {
	private final FlightService flightService;
	
	public InternalFlightController(FlightService flightService) {
		this.flightService = flightService;
	}
	
	@GetMapping("/{id}")
    public FlightSummaryDto getFlightById(@PathVariable int id) {
        return flightService.getFlightById(id);
    }

    @PutMapping("/{id}/seats")
    public void updateSeats(@PathVariable int id, @RequestParam int count) {
        flightService.updateSeats(id, count);
    }
}
