package com.flightapp.flightservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flightapp.flightservice.entity.Airline;

public interface AirlineRepository extends JpaRepository<Airline, Integer>{
	
	Optional<Airline> findByCode(String code);
	
	boolean existsByCode(String code);
}
