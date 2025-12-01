package com.flightbookingservice.implTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.flightbookingservice.dto.BookingRequest;
import com.flightbookingservice.dto.FlightSummaryDto;
import com.flightbookingservice.dto.ItineraryDto;
import com.flightbookingservice.dto.PassengerRequest;
import com.flightbookingservice.entity.Booking;
import com.flightbookingservice.entity.BookingStatus;
import com.flightbookingservice.entity.Gender;
import com.flightbookingservice.entity.Itinerary;
import com.flightbookingservice.entity.MealType;
import com.flightbookingservice.entity.Passenger;
import com.flightbookingservice.entity.Role;
import com.flightbookingservice.entity.TripSegmentType;
import com.flightbookingservice.entity.TripType;
import com.flightbookingservice.entity.User;
import com.flightbookingservice.exception.CancellationNotAllowedException;
import com.flightbookingservice.exception.SeatNotAvailableException;
import com.flightbookingservice.feignclient.FlightClient;
import com.flightbookingservice.repository.ItineraryRepository;
import com.flightbookingservice.repository.UserRepository;
import com.flightbookingservice.service.implimentation.BookingServiceImpl;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItineraryRepository itineraryRepository;

    @Mock
    private FlightClient flightClient;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User user;
    private FlightSummaryDto outwardFlightDto;
    private FlightSummaryDto returnFlightDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setName("Tanmay");
        user.setEmail("tanmay@example.com");
        user.setRole(Role.USER);

        outwardFlightDto = new FlightSummaryDto();
        outwardFlightDto.setFlightId(101);
        outwardFlightDto.setFromAirport("DEL");
        outwardFlightDto.setToAirport("BOM");
        outwardFlightDto.setDepartureTime(LocalDateTime.now().plusDays(3));
        outwardFlightDto.setArrivalTime(LocalDateTime.now().plusDays(3).plusHours(2));
        outwardFlightDto.setPrice(5000);
        outwardFlightDto.setAvailableSeats(180);

        returnFlightDto = new FlightSummaryDto();
        returnFlightDto.setFlightId(202);
        returnFlightDto.setFromAirport("BOM");
        returnFlightDto.setToAirport("DEL");
        returnFlightDto.setDepartureTime(LocalDateTime.now().plusDays(7));
        returnFlightDto.setArrivalTime(LocalDateTime.now().plusDays(7).plusHours(2));
        returnFlightDto.setPrice(5500);
        returnFlightDto.setAvailableSeats(180);
    }

    private PassengerRequest buildPassenger(String name, Gender gender, int age, MealType mealType, String seat) {
        PassengerRequest p = new PassengerRequest();
        p.setName(name);
        p.setGender(gender);
        p.setAge(age);
        p.setMealType(mealType);
        p.setSeatNumber(seat);
        return p;
    }

    private BookingRequest buildOneWayRequest() {
        BookingRequest req = new BookingRequest();
        req.setName("Tanmay Dhelia");
        req.setEmail("tanmay@example.com");
        req.setTripType(TripType.ONE_WAY);
        req.setReturnFlightId(null);
        req.setNumberOfSeats(1);
        req.setPassengers(List.of(
                buildPassenger("Tanmay Dhelia", Gender.MALE, 22, MealType.VEG, "12A")
        ));
        return req;
    }

    private BookingRequest buildRoundTripRequest() {
        BookingRequest req = buildOneWayRequest();
        req.setTripType(TripType.ROUND_TRIP);
        req.setReturnFlightId(202);
        return req;
    }

    @Test
    void bookItinerary_oneWay_success() {
        BookingRequest request = buildOneWayRequest();

        when(flightClient.getFlightById(101)).thenReturn(outwardFlightDto);
        when(userRepository.findByEmail("tanmay@example.com")).thenReturn(Optional.of(user));
        
        // Mocking the call to update seats (returns void, so doNothing is default for mocks, but explicit is fine)
        doNothing().when(flightClient).updateSeats(eq(101), eq(-1));

        when(itineraryRepository.save(any(Itinerary.class))).thenAnswer(inv -> {
            Itinerary i = inv.getArgument(0);
            i.setId(1);
            // Simulate bookings being saved with IDs
            i.getBookings().get(0).setId(10); 
            return i;
        });
        
        // Mock client calls inside toItineraryDto mapping
        // The service calls getFlightById again inside the mapper
        when(flightClient.getFlightById(101)).thenReturn(outwardFlightDto);

        ItineraryDto dto = bookingService.bookItinerary(101, request);

        assertNotNull(dto.getPnr());
        assertEquals("BOOKED", dto.getStatus().toString());
        assertEquals(5000, dto.getTotalAmount());
        assertEquals(1, dto.getLegs().size());
        
        // Verify seat update call
        verify(flightClient, times(1)).updateSeats(101, -1);
    }

    @Test
    void bookItinerary_roundTrip_success() {
        BookingRequest request = buildRoundTripRequest();

        when(flightClient.getFlightById(101)).thenReturn(outwardFlightDto);
        when(flightClient.getFlightById(202)).thenReturn(returnFlightDto);
        when(userRepository.findByEmail("tanmay@example.com")).thenReturn(Optional.of(user));
        when(itineraryRepository.save(any(Itinerary.class))).thenAnswer(inv -> inv.getArgument(0));

        // Mock calls inside toItineraryDto mapping
        // Note: Mockito remembers previous stubs, but explicit is safe
        when(flightClient.getFlightById(101)).thenReturn(outwardFlightDto);
        when(flightClient.getFlightById(202)).thenReturn(returnFlightDto);

        ItineraryDto dto = bookingService.bookItinerary(101, request);

        assertEquals(BookingStatus.BOOKED, dto.getStatus());
        assertEquals(10500, dto.getTotalAmount()); // 5000 + 5500
        assertEquals(2, dto.getLegs().size());
        
        verify(flightClient).updateSeats(101, -1);
        verify(flightClient).updateSeats(202, -1);
    }

    @Test
    void bookItinerary_notEnoughSeats_throwsException() {
        BookingRequest request = buildOneWayRequest();
        outwardFlightDto.setAvailableSeats(0); // No seats

        when(flightClient.getFlightById(101)).thenReturn(outwardFlightDto);

        assertThrows(SeatNotAvailableException.class, () -> bookingService.bookItinerary(101, request));
        
        verify(flightClient, never()).updateSeats(anyInt(), anyInt());
        verify(itineraryRepository, never()).save(any());
    }
    
    @Test
    void bookItinerary_duplicateSeatRequest_throwsException() {
        BookingRequest request = buildOneWayRequest();
        request.setNumberOfSeats(2);
        request.setPassengers(List.of(
            buildPassenger("P1", Gender.MALE, 20, MealType.VEG, "12A"),
            buildPassenger("P2", Gender.FEMALE, 22, MealType.VEG, "12A") // Duplicate
        ));
        
        when(flightClient.getFlightById(101)).thenReturn(outwardFlightDto);

        assertThrows(SeatNotAvailableException.class, () -> bookingService.bookItinerary(101, request));
    }

    @Test
    void getItineraryByPnr_success() {
        Itinerary itinerary = new Itinerary();
        itinerary.setPnr("PNR123");
        itinerary.setUser(user);
        itinerary.setStatus(BookingStatus.BOOKED);
        itinerary.setCreatedTime(LocalDateTime.now());
        itinerary.setTotalAmount(5000);

        Booking booking = new Booking();
        booking.setId(10);
        booking.setFlightId(101); // Only ID stored
        booking.setSegmentType(TripSegmentType.ONE_WAY);
        booking.setStatus(BookingStatus.BOOKED);
        booking.setPassengers(List.of(new Passenger()));
        
        itinerary.setBookings(List.of(booking));

        when(itineraryRepository.findByPnr("PNR123")).thenReturn(Optional.of(itinerary));
        
        // Mock call needed for mapping DTO
        when(flightClient.getFlightById(101)).thenReturn(outwardFlightDto);

        ItineraryDto dto = bookingService.getItineraryByPnr("PNR123");
        
        assertEquals("PNR123", dto.getPnr());
        assertEquals("DEL", dto.getLegs().get(0).getFromAirport());
    }

    @Test
    void getHistoryByEmail_success() {
        Itinerary it1 = new Itinerary();
        it1.setPnr("P1");
        it1.setUser(user);
        it1.setBookings(Collections.emptyList());

        when(itineraryRepository.findByUserEmail("tanmay@example.com")).thenReturn(List.of(it1));

        List<ItineraryDto> result = bookingService.getHistoryByEmail("tanmay@example.com");
        
        assertEquals(1, result.size());
        assertEquals("P1", result.get(0).getPnr());
    }

    @Test
    void cancelByPnr_success() {
        Itinerary itinerary = new Itinerary();
        itinerary.setPnr("PNR123");
        itinerary.setUser(user);
        itinerary.setStatus(BookingStatus.BOOKED);

        Booking booking = new Booking();
        booking.setFlightId(101);
        booking.setStatus(BookingStatus.BOOKED);
        // 2 passengers
        booking.setPassengers(List.of(new Passenger(), new Passenger()));
        
        itinerary.setBookings(List.of(booking));

        when(itineraryRepository.findByPnr("PNR123")).thenReturn(Optional.of(itinerary));
        
        // Mock flight fetch for cancellation time check
        when(flightClient.getFlightById(101)).thenReturn(outwardFlightDto);

        bookingService.cancelByPnr("PNR123");

        assertEquals(BookingStatus.CANCELLED, itinerary.getStatus());
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        
        // Verify we release 2 seats
        verify(flightClient).updateSeats(101, 2);
    }
    
    @Test
    void cancelByPnr_lateCancellation_throwsException() {
        // Departure is in 1 hour
        outwardFlightDto.setDepartureTime(LocalDateTime.now().plusHours(1));
        
        Itinerary itinerary = new Itinerary();
        itinerary.setPnr("PNR123");
        
        Booking booking = new Booking();
        booking.setFlightId(101);
        booking.setStatus(BookingStatus.BOOKED);
        itinerary.setBookings(List.of(booking));

        when(itineraryRepository.findByPnr("PNR123")).thenReturn(Optional.of(itinerary));
        when(flightClient.getFlightById(101)).thenReturn(outwardFlightDto);

        assertThrows(CancellationNotAllowedException.class, () -> bookingService.cancelByPnr("PNR123"));
        
        verify(flightClient, never()).updateSeats(anyInt(), anyInt());
    }
}