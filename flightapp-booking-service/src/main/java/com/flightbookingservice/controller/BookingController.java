package com.flightbookingservice.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flightbookingservice.dto.BookingRequest;
import com.flightbookingservice.dto.CancelResponse;
import com.flightbookingservice.dto.ItineraryDto;
import com.flightbookingservice.service.BookingService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api")
public class BookingController {
	private final BookingService bookingService;
	
	public BookingController(BookingService bookingService) {
		super();
		this.bookingService = bookingService;
	}
	
	@PostMapping("/book/{flightId}")
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
	
	@GetMapping("/history/{emailId}")
	public List<ItineraryDto> getBookinghistory(@PathVariable String emailId){
		log.info("GET /api/v1.0/flight/booking/history/{}",emailId);
		return bookingService.getHistoryByEmail(emailId);
	}
	
	@DeleteMapping("/cancel/{pnr}")
	public CancelResponse cancelBooking(@PathVariable String pnr) {
		log.info("DELETE /api/v1.0/flight/booking/cancel/{}",pnr);
		return bookingService.cancelByPnr(pnr);
	}
}
