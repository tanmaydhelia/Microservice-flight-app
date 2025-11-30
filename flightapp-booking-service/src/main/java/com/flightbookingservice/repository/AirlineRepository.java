package com.flightapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flightapp.entity.Airline;

public interface AirlineRepository extends JpaRepository<Airline, Integer>{
	
	Optional<Airline> findByCode(String code);
	
	boolean existsByCode(String code);
}
