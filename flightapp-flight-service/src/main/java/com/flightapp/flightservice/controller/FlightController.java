package com.flightapp.flightservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.flightservice.dto.request.FlightSearchRequest;
import com.flightapp.flightservice.dto.response.FlightSummaryDto;
import com.flightapp.flightservice.service.FlightService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/v1.0/flight")
public class FlightController {
	private final FlightService flightService;
	
	public FlightController(FlightService flightService) {
		this.flightService = flightService;
	}
	
	@PostMapping("/search")
	@ResponseStatus(code = HttpStatus.CREATED)
	public List<FlightSummaryDto> searchFlights(@RequestBody @Valid FlightSearchRequest req){
		log.info("POST /api/v1.0/flight/search from={} to={} date={} tripType={}", req.getFrom(), req.getTo(), req.getJourneyDate(), req.getTripType());
		return flightService.searchFlights(req);
	}
}
