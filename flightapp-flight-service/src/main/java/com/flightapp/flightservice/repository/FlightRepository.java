package com.flightapp.flightservice.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flightapp.flightservice.entity.Flight;
import com.flightapp.flightservice.entity.FlightStatus;

public interface FlightRepository extends JpaRepository<Flight, Integer>{
	
	List<Flight> findByFromAirportAndToAirportAndDepartureTimeBetweenAndStatus(
		String fromAirport,
		String toAirport,
		LocalDateTime departureStart,
		LocalDateTime departureEnd,
		FlightStatus status
	);
}
