package com.flightbookingservice.controllerTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightbookingservice.controller.BookingController;
import com.flightbookingservice.dto.BookingRequest;
import com.flightbookingservice.dto.ItineraryDto;
import com.flightbookingservice.dto.PassengerRequest;
import com.flightbookingservice.entity.Gender;
import com.flightbookingservice.entity.MealType;
import com.flightbookingservice.entity.TripType;
import com.flightbookingservice.exception.SeatNotAvailableException;
import com.flightbookingservice.service.BookingService;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void bookTicket_success() throws Exception {
        BookingRequest req = new BookingRequest();
        req.setName("Test");
        req.setEmail("test@test.com");
        req.setTripType(TripType.ONE_WAY);
        req.setNumberOfSeats(1);
        
        PassengerRequest p = new PassengerRequest();
        p.setName("P1");
        p.setGender(Gender.MALE);
        p.setAge(25);
        p.setMealType(MealType.VEG);
        p.setSeatNumber("1A");
        req.setPassengers(List.of(p));

        when(bookingService.bookItinerary(eq(101), any())).thenReturn(new ItineraryDto());

        mockMvc.perform(post("/api/v1.0/flight/booking/101")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
    
    @Test
    void bookTicket_serviceThrowsException_returnsConflict() throws Exception {
        BookingRequest req = new BookingRequest();
        // ... populate valid request ...
        req.setName("Test");
        req.setEmail("test@test.com");
        req.setTripType(TripType.ONE_WAY);
        req.setNumberOfSeats(1);
        PassengerRequest p = new PassengerRequest();
        p.setName("P1");
        p.setGender(Gender.MALE);
        p.setAge(25);
        p.setMealType(MealType.VEG);
        p.setSeatNumber("1A");
        req.setPassengers(List.of(p));

        when(bookingService.bookItinerary(eq(101), any()))
            .thenThrow(new SeatNotAvailableException("No seats"));

        mockMvc.perform(post("/api/v1.0/flight/booking/101")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict()); // 409
    }
}