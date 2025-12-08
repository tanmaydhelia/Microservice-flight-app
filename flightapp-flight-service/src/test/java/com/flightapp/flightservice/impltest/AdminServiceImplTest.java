package com.flightapp.flightservice.impltest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.flightapp.flightservice.dto.request.AirlineInventoryRequest;
import com.flightapp.flightservice.dto.request.FlightInventoryItemDto;
import com.flightapp.flightservice.dto.response.AirlineInventoryResponse;
import com.flightapp.flightservice.entity.Airline;
import com.flightapp.flightservice.entity.Flight;
import com.flightapp.flightservice.implimentation.AdminServiceImpl;
import com.flightapp.flightservice.repository.AirlineRepository;
import com.flightapp.flightservice.repository.FlightRepository;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private AirlineRepository airlineRepository;

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    private Airline airline;

    @BeforeEach
    void setUp() {
        airline = new Airline();
        airline.setId(1);
        airline.setCode("AI");
        airline.setName("Air India");
    }

    @Test
    void addInventory_success() {
        AirlineInventoryRequest request = new AirlineInventoryRequest();
        request.setAirlineCode("AI");

        FlightInventoryItemDto item = new FlightInventoryItemDto();
        item.setFromAirport("DEL");
        item.setToAirport("BOM");
        item.setDepartureTime(LocalDateTime.now().plusDays(1));
        item.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(2));
        item.setPrice(5000);
        item.setTotalSeats(180);

        request.setFlights(List.of(item));

        when(airlineRepository.findByCode("AI")).thenReturn(Optional.of(airline));
        when(flightRepository.saveAll(any())).thenAnswer(invocation -> {
            List<Flight> flights = invocation.getArgument(0);
            flights.get(0).setId(101);
            return flights;
        });

        AirlineInventoryResponse response = adminService.addInventory(request);

        assertEquals("AI", response.getAirLineCode());
        assertEquals(1, response.getFlightsAdded());
        assertEquals(101, response.getFlightIds().get(0));
    }

//    @Test
//    void addInventory_airlineNotFound_throwsException() {
//        AirlineInventoryRequest request = new AirlineInventoryRequest();
//        request.setAirlineCode("XX");
//
//        when(airlineRepository.findByCode("XX")).thenReturn(Optional.empty());
//
//        assertThrows(ResourceNotFoundException.class, () -> adminService.addInventory(request));
//    }

    @Test
    void addInventory_invalidTime_throwsException() {
        AirlineInventoryRequest request = new AirlineInventoryRequest();
        request.setAirlineCode("AI");
        
        FlightInventoryItemDto item = new FlightInventoryItemDto();
        item.setDepartureTime(LocalDateTime.now());
        item.setArrivalTime(LocalDateTime.now().minusHours(2)); // Arrival before Dept
        item.setTotalSeats(100);
        
        request.setFlights(List.of(item));

        when(airlineRepository.findByCode("AI")).thenReturn(Optional.of(airline));

        assertThrows(IllegalArgumentException.class, () -> adminService.addInventory(request));
    }
    
    @Test
    void addInventory_zeroSeats_throwsException() {
        AirlineInventoryRequest request = new AirlineInventoryRequest();
        request.setAirlineCode("AI");
        
        FlightInventoryItemDto item = new FlightInventoryItemDto();
        item.setDepartureTime(LocalDateTime.now());
        item.setArrivalTime(LocalDateTime.now().plusHours(2));
        item.setTotalSeats(0); // Invalid
        
        request.setFlights(List.of(item));

        when(airlineRepository.findByCode("AI")).thenReturn(Optional.of(airline));

        assertThrows(IllegalArgumentException.class, () -> adminService.addInventory(request));
    }
}