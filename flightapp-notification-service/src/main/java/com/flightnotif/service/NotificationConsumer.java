package com.flightnotif.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.flightnotif.dto.BookingCancelledEvent;
import com.flightnotif.dto.BookingPlacedEvent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationConsumer {
	@KafkaListener(topics = "booking-cancellation-topic", groupId = "notification-group")
    public void handleCancellation(BookingCancelledEvent event) {
        // In a real app, this would send an email via SMTP
        log.info("==================================================");
        log.info("📧 SENDING CANCELLATION EMAIL");
        log.info("To User with PNR: {}", event.getPnr());
        log.info("Message: Your flight (ID: {}) has been cancelled.", event.getFlightId());
        log.info("==================================================");
    }

    // 2. Listen for New Bookings (We will trigger this next)
    @KafkaListener(topics = "booking-placed-topic", groupId = "notification-group")
    public void handleNewBooking(BookingPlacedEvent event) {
        log.info("==================================================");
        log.info("📧 SENDING BOOKING CONFIRMATION");
        log.info("To: {} ({})", event.getName(), event.getEmail());
        log.info("PNR: {}", event.getPnr());
        log.info("Message: Your journey is confirmed!");
        log.info("==================================================");
    }
}
