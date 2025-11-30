package com.flightbookingservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flightbookingservice.entity.Itinerary;

public interface ItineraryRepository extends JpaRepository<Itinerary, Integer> {
	Optional<Itinerary> findByPnr(String pnr);
	
	List<Itinerary> findByUserEmail(String email); 
}
