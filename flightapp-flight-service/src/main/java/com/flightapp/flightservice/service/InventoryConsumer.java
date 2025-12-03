package com.flightapp.flightservice.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.flightapp.flightservice.dto.request.BookingCancelledEvent;
import com.flightapp.flightservice.entity.Flight;
import com.flightapp.flightservice.repository.FlightRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InventoryConsumer {
	
	private final FlightRepository flightRepository;
	
	public InventoryConsumer(FlightRepository flightRepository) {
		this.flightRepository = flightRepository;
	}

	
	@KafkaListener(topics = "booking-cancellation-topic", groupId = "flight-inventory-group")
	public void handleCancellation(BookingCancelledEvent event) {
		log.info("Received cancellation event for Flight ID: {}", event.getFlightId());
		
		Flight flight = flightRepository.findById(event.getFlightId()).orElseThrow();
        flight.setAvailableSeats(flight.getAvailableSeats() + event.getSeatsToRelease());
        flightRepository.save(flight);
	}
}
