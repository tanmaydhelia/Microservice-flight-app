package com.flightbookingservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flightbookingservice.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Integer>{
	
	List<Booking> findByItineraryId(int itineraryId);
}
