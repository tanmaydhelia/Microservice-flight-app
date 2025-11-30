package com.flightbookingservice.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.flightbookingservice.dto.FlightSummaryDto;

@FeignClient(name = "FLIGHTAPP-FLIGHT-SERVICE")
public interface FlightClient {
	
	@GetMapping("/api/internal/flight/{id}")
	FlightSummaryDto getFlightById(@PathVariable int id);
	
	@PutMapping("/api/internal/flight/{id}/seats")
	void updateSeats(@PathVariable int id, @RequestParam int count);
}
