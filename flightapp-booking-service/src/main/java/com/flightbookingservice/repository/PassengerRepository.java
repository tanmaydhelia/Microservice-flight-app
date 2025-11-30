package com.flightapp.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.flightapp.entity.Passenger;

public interface PassengerRepository extends JpaRepository<Passenger, Integer>{
	
	@Query("select p.seatNumber from Passenger p where p.booking.flight.id = :flightId and p.seatNumber in :seatNumbers ")
	List<String> findTakenSeatNumbers(int flightId, Collection<String> seatNumbers);
	
	List<Passenger> findByBookingId(int bookingId);
}
