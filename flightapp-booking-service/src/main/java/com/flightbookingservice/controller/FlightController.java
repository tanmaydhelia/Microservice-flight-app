package com.flightbookingservice.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.dto.BookingRequest;
import com.flightapp.dto.CancelResponse;
import com.flightapp.dto.FlightSearchRequest;
import com.flightapp.dto.FlightSummaryDto;
import com.flightapp.dto.ItineraryDto;
import com.flightapp.service.BookingService;
import com.flightapp.service.FlightService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/v1.0/flight")
public class FlightController {
	private final FlightService flightService;
	private final BookingService bookingService;
	
	public FlightController(FlightService flightService, BookingService bookingService) {
		super();
		this.flightService = flightService;
		this.bookingService = bookingService;
	}
	
	@PostMapping("/search")
	public List<FlightSummaryDto> searchFlights(@RequestBody @Valid FlightSearchRequest req){
		log.info("POST /api/v1.0/flight/search from={} to={} date={} tripType={}", req.getFrom(), req.getTo(), req.getJourneyDate(), req.getTripType());
		return flightService.searchFlights(req);
	}
	
	@PostMapping("/booking/{flightId}")
	public ItineraryDto bookTicket(@PathVariable int flightId, @RequestBody @Valid BookingRequest req) {
		log.info("POST /api/v1.0/flight/booking/{} for email={} tripType={}",
                flightId, req.getEmail(), req.getTripType());
		
		return bookingService.bookItinerary(flightId, req);
	}
	
	@GetMapping("/ticket/{pnr}")
	public ItineraryDto getTicketByPnr(@PathVariable String pnr) {
		log.info("GET /api/v1.0/flight/ticket/{}",pnr);
		return bookingService.getItineraryByPnr(pnr);
	}
	
	@GetMapping("/booking/history/{emailId}")
	public List<ItineraryDto> getBookinghistory(@PathVariable String emailId){
		log.info("GET /api/v1.0/flight/booking/history/{}",emailId);
		return bookingService.getHistoryByEmail(emailId);
	}
	
	@DeleteMapping("/booking/cancel/{pnr}")
	public CancelResponse cancelBooking(@PathVariable String pnr) {
		log.info("DELETE /api/v1.0/flight/booking/cancel/{}",pnr);
		return bookingService.cancelByPnr(pnr);
	}
}
