package com.flightapp.flightservice.impltest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.flightapp.flightservice.dto.request.FlightSearchRequest;
import com.flightapp.flightservice.dto.response.FlightSummaryDto;
import com.flightapp.flightservice.entity.Airline;
import com.flightapp.flightservice.entity.Flight;
import com.flightapp.flightservice.entity.FlightStatus;
import com.flightapp.flightservice.entity.TripType;
import com.flightapp.flightservice.exception.ResourceNotFoundException;
import com.flightapp.flightservice.exception.SeatNotAvailableException;
import com.flightapp.flightservice.implimentation.FlightServiceImpl;
import com.flightapp.flightservice.repository.FlightRepository;

@ExtendWith(MockitoExtension.class)
class FlightServiceImplTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private FlightServiceImpl flightService;

    private Flight flight;
    private Airline airline;

    @BeforeEach
    void setUp() {
        airline = new Airline();
        airline.setId(1);
        airline.setName("Air India");
        airline.setCode("AI");

        flight = new Flight();
        flight.setId(101);
        flight.setAirline(airline);
        flight.setFromAirport("DEL");
        flight.setToAirport("BOM");
        flight.setDepartureTime(LocalDateTime.now().plusDays(1).withHour(10));
        flight.setArrivalTime(LocalDateTime.now().plusDays(1).withHour(12));
        flight.setPrice(5000);
        flight.setTotalSeats(180);
        flight.setAvailableSeats(100);
        flight.setStatus(FlightStatus.SCHEDULED);
    }

    @Test
    void searchFlights_success() {
        FlightSearchRequest request = new FlightSearchRequest();
        request.setFrom("DEL");
        request.setTo("BOM");
        request.setJourneyDate(LocalDate.now().plusDays(1));
        request.setTripType(TripType.ONE_WAY);

        when(flightRepository.findByFromAirportAndToAirportAndDepartureTimeBetweenAndStatus(
                eq("DEL"), eq("BOM"), any(LocalDateTime.class), any(LocalDateTime.class), eq(FlightStatus.SCHEDULED)
        )).thenReturn(List.of(flight));

        List<FlightSummaryDto> results = flightService.searchFlights(request);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("AI", results.get(0).getAirlineCode());
        assertEquals(100, results.get(0).getAvailableSeats()); // Checking mapped field
    }

    @Test
    void getFlightById_success() {
        when(flightRepository.findById(101)).thenReturn(Optional.of(flight));

        FlightSummaryDto dto = flightService.getFlightById(101);

        assertNotNull(dto);
        assertEquals(101, dto.getFlightId());
        assertEquals("DEL", dto.getFromAirport());
    }

    @Test
    void getFlightById_notFound_throwsException() {
        when(flightRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> flightService.getFlightById(999));
    }

    @Test
    void updateSeats_booking_success() {
        when(flightRepository.findById(101)).thenReturn(Optional.of(flight));

        // Decrease seats by 2
        flightService.updateSeats(101, -2);

        assertEquals(98, flight.getAvailableSeats());
        verify(flightRepository, times(1)).save(flight);
    }

    @Test
    void updateSeats_cancellation_success() {
        when(flightRepository.findById(101)).thenReturn(Optional.of(flight));

        // Increase seats by 2
        flightService.updateSeats(101, 2);

        assertEquals(102, flight.getAvailableSeats());
        verify(flightRepository, times(1)).save(flight);
    }

    @Test
    void updateSeats_capAtTotalSeats() {
        flight.setAvailableSeats(179);
        when(flightRepository.findById(101)).thenReturn(Optional.of(flight));

        // Try to add 5 seats (179 + 5 = 184), should cap at 180
        flightService.updateSeats(101, 5);

        assertEquals(180, flight.getAvailableSeats());
        verify(flightRepository, times(1)).save(flight);
    }

    @Test
    void updateSeats_notEnoughSeats_throwsException() {
        flight.setAvailableSeats(1);
        when(flightRepository.findById(101)).thenReturn(Optional.of(flight));

        // Try to book 2 seats
        assertThrows(SeatNotAvailableException.class, () -> flightService.updateSeats(101, -2));
        
        // Ensure save is never called
        verify(flightRepository, never()).save(any());
    }
    
    @Test
    void updateSeats_flightNotFound_throwsException() {
        when(flightRepository.findById(999)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> flightService.updateSeats(999, -1));
    }
}