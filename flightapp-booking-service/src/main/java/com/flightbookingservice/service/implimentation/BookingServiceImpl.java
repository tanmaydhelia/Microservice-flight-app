package com.flightbookingservice.service.implimentation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flightbookingservice.dto.BookingCancelledEvent;
import com.flightbookingservice.dto.BookingPlacedEvent;
import com.flightbookingservice.dto.BookingRequest;
import com.flightbookingservice.dto.CancelResponse;
import com.flightbookingservice.dto.FlightSummaryDto;
import com.flightbookingservice.dto.ItineraryDto;
import com.flightbookingservice.dto.LegDto;
import com.flightbookingservice.dto.PassengerDto;
import com.flightbookingservice.dto.PassengerRequest;
import com.flightbookingservice.entity.Booking;
import com.flightbookingservice.entity.BookingStatus;
import com.flightbookingservice.entity.Itinerary;
import com.flightbookingservice.entity.Passenger;
import com.flightbookingservice.entity.Role;
import com.flightbookingservice.entity.TripSegmentType;
import com.flightbookingservice.entity.TripType;
import com.flightbookingservice.entity.User;
import com.flightbookingservice.exception.ResourceNotFoundException;
import com.flightbookingservice.exception.SeatNotAvailableException;
import com.flightbookingservice.feignclient.FlightClient;
import com.flightbookingservice.repository.ItineraryRepository;
import com.flightbookingservice.repository.UserRepository;
import com.flightbookingservice.service.BookingService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {
	
	@Autowired
	private KafkaTemplate<String, BookingCancelledEvent> cancelTemplate;
   
	@Autowired
	private KafkaTemplate<String, BookingPlacedEvent> placedTemplate;
	
	private final UserRepository userRepository;
    private final ItineraryRepository itineraryRepository;
    private final FlightClient flightClient; // Replaces FlightRepository
   

    public BookingServiceImpl(UserRepository userRepository, FlightClient flightClient,
                              ItineraryRepository itineraryRepository) {
        this.userRepository = userRepository;
        this.flightClient = flightClient;
        this.itineraryRepository = itineraryRepository;
    }

    @Override
    @Transactional
    public ItineraryDto bookItinerary(int outwardFlightId, BookingRequest req) {
        log.info("Booking itinerary for email={} tripType={} outwardFlight={}", req.getEmail(), req.getTripType(), outwardFlightId);

        validateBookingReq(req);

        // 1. Fetch Outward Flight Details via Network
        FlightSummaryDto outwardFlight = flightClient.getFlightById(outwardFlightId);

        FlightSummaryDto returnFlight = null;
        boolean isRoundTrip = req.getTripType() == TripType.ROUND_TRIP;

        if (isRoundTrip) {
            if (req.getReturnFlightId() == null) {
                throw new IllegalArgumentException("Return FlightId required!!!");
            }
            returnFlight = flightClient.getFlightById(req.getReturnFlightId());
        }

        int seats = req.getNumberOfSeats();
        if (outwardFlight.getAvailableSeats() < seats) {
            throw new SeatNotAvailableException("Not enough seats available on outward flight");
        }
        if (isRoundTrip && returnFlight.getAvailableSeats() < seats) {
            throw new SeatNotAvailableException("Not enough seats available on return flight");
        }

        // Duplicate seat check
        Set<String> seatNumbers = new HashSet<>();
        for (PassengerRequest pr : req.getPassengers()) {
            if (!seatNumbers.add(pr.getSeatNumber())) {
                throw new SeatNotAvailableException("Duplicate seat number in request: " + pr.getSeatNumber());
            }
        }

        try {
            flightClient.updateSeats(outwardFlightId, -seats);
            if (isRoundTrip) {
                flightClient.updateSeats(req.getReturnFlightId(), -seats);
            }
        } catch (Exception e) {
     
            log.error("Failed to update seats via Flight Service", e);
            throw new SeatNotAvailableException("Could not reserve seats. Please try again.");
        }

        // Calculate Totals
        int outwardAmount = outwardFlight.getPrice() * seats;
        int returnAmount = isRoundTrip ? (returnFlight.getPrice() * seats) : 0;
        int totalAmount = outwardAmount + returnAmount;

        User user = getOrCreateUser(req.getName(), req.getEmail());

        Itinerary i = new Itinerary();
        i.setPnr(generatePnr());
        i.setUser(user);
        i.setCreatedTime(LocalDateTime.now());
        i.setTotalAmount(totalAmount);
        i.setStatus(BookingStatus.BOOKED);

        List<Booking> bookings = new ArrayList<>();

        Booking outwardBooking = createBookingLeg(i, outwardFlight, req, isRoundTrip ? TripSegmentType.OUTBOUND : TripSegmentType.ONE_WAY);
        bookings.add(outwardBooking);
        
        if (isRoundTrip && returnFlight != null) {
            Booking returnBooking = createBookingLeg(i, returnFlight, req, TripSegmentType.RETURN);
            bookings.add(returnBooking);
        }
        
        i.setBookings(bookings);
        itineraryRepository.save(i);

        log.info("Itinerary added successfully with PNR={} for user={}", i.getPnr(), user.getName());

        try {
            BookingPlacedEvent event = new BookingPlacedEvent();
            event.setPnr(i.getPnr());
            event.setEmail(user.getEmail());
            event.setName(user.getName());
            
            placedTemplate.send("booking-placed-topic", event);
            log.info("Published BookingPlacedEvent for PNR: {}", i.getPnr());
        } catch (Exception e) {
            log.error("Failed to publish booking event (non-fatal)", e);
        }
        
        return toItineraryDto(i);
    }

    private void validateBookingReq(BookingRequest req) {
        if (req.getPassengers() == null || req.getPassengers().isEmpty()) {
            throw new IllegalArgumentException("Add Atleast one passenger!!!");
        }
        if (req.getNumberOfSeats() == 0) {
            throw new IllegalArgumentException("No Seats requested!!!");
        }
        if (req.getPassengers().size() != req.getNumberOfSeats()) {
            throw new IllegalArgumentException("Number of seats != number of passengers!!!");
        }
    }

    private User getOrCreateUser(String name, String email) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            log.info("User Not found for email={}, hence CREATING NEW USER", email);
            User u = new User();
            u.setName(name);
            u.setEmail(email);
            u.setRole(Role.USER);
            return userRepository.save(u);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ItineraryDto getItineraryByPnr(String pnr) {
        log.info("Fetching itinerary by PNR={}", pnr);
        Itinerary i = itineraryRepository.findByPnr(pnr)
                .orElseThrow(() -> new ResourceNotFoundException("No itinerary for pnr!!!"));
        return toItineraryDto(i);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItineraryDto> getHistoryByEmail(String email) {
        log.info("Fetching booking history for email={}", email);
        List<Itinerary> i = itineraryRepository.findByUserEmail(email);
        log.debug("Found {} itineraries", i.size());
        return i.stream().map(this::toItineraryDto).toList();
    }

//Cancellation by Feign Client using Flight Service    
//    @Override
//    @Transactional
//    public CancelResponse cancelByPnr(String pnr) {
//        log.info("Attempting Cancel for pnr = {}", pnr);
//
//        Itinerary i = itineraryRepository.findByPnr(pnr)
//                .orElseThrow(() -> new ResourceNotFoundException("Itinerary not found for PNR: " + pnr));
//
//        LocalDateTime curr = LocalDateTime.now();
//
//        // Validate cancellation policy by fetching Flight info
//        for (Booking b : i.getBookings()) {
//            if (b.getStatus() != BookingStatus.BOOKED) {
//                continue;
//            }
//
//            // Fetch flight details to get current Departure Time
//            FlightSummaryDto flightDto = flightClient.getFlightById(b.getFlightId());
//            LocalDateTime deptTime = flightDto.getDepartureTime();
//
//            if (curr.plusHours(24).isAfter(deptTime)) {
//                log.warn("Cancellation not Allowed for pnr={}", pnr);
//                throw new CancellationNotAllowedException("Cannot Cancel as booking within 24hrs");
//            }
//        }
//
//        // Process Cancellation
//        for (Booking b : i.getBookings()) {
//            if (b.getStatus() == BookingStatus.BOOKED) {
//                b.setStatus(BookingStatus.CANCELLED);
//                
//                int passengerCount = b.getPassengers().size();
//                
//                // Release seats via Network Call (+ve value)
//                flightClient.updateSeats(b.getFlightId(), passengerCount);
//            }
//        }
//
//        i.setStatus(BookingStatus.CANCELLED);
//        itineraryRepository.save(i);
//
//        log.info("Cancellation Successful for pnr={}!!!", pnr);
//
//        CancelResponse cr = new CancelResponse();
//        cr.setPnr(pnr);
//        cr.setStatus(BookingStatus.CANCELLED);
//        cr.setMessage("Booking Cancelled Successfully!!!");
//        return cr;
//    }
    
    //Cancellation via Kafka event
    @Override
    @Transactional
    public CancelResponse cancelByPnr(String pnr) {
    	log.info("Attempting Cancel for pnr = {}", pnr);

    	Itinerary i = itineraryRepository.findByPnr(pnr)
    			.orElseThrow(() -> new ResourceNotFoundException("Itinerary not found for PNR: " + pnr));
    	
    	// 1. Update Local DB Status
        i.setStatus(BookingStatus.CANCELLED);
       
        for (Booking b : i.getBookings()) {
            b.setStatus(BookingStatus.CANCELLED);
        }
        
        itineraryRepository.save(i);
        
        for (Booking b : i.getBookings()) {
            BookingCancelledEvent event = new BookingCancelledEvent();
            event.setPnr(pnr);
            event.setFlightId(b.getFlightId());
            event.setSeatsToRelease(b.getPassengers().size());
            
            // Publish to Kafka
            cancelTemplate.send("booking-cancellation-topic", event);
       }

       log.info("Cancellation processed locally for pnr={}. Events published.", pnr);
        
       // 3. Return response immediately
       CancelResponse cr = new CancelResponse();
       cr.setPnr(pnr);
       cr.setStatus(BookingStatus.CANCELLED);
       cr.setMessage("Booking Cancelled Successfully!!!");
       return cr;
    }

    // Updated to accept FlightSummaryDto instead of Flight Entity
    private Booking createBookingLeg(Itinerary it, FlightSummaryDto flightDto, BookingRequest req, TripSegmentType segmentType) {
        Booking booking = new Booking();
        booking.setItinerary(it);
        booking.setFlightId(flightDto.getFlightId()); // Setting ID, not object
        booking.setJourneyDate(flightDto.getDepartureTime().toLocalDate());
        booking.setSegmentType(segmentType);
        booking.setStatus(BookingStatus.BOOKED);

        List<Passenger> passengers = new ArrayList<>();
        for (PassengerRequest pr : req.getPassengers()) {
            Passenger p = new Passenger();
            p.setBooking(booking);
            p.setName(pr.getName());
            p.setGender(pr.getGender());
            p.setAge(pr.getAge());
            p.setMealType(pr.getMealType());
            p.setSeatNumber(pr.getSeatNumber());
            passengers.add(p);
        }
        booking.setPassengers(passengers);

        return booking;
    }

    private String generatePnr() {
        String uuid = UUID.randomUUID().toString().replace("-", " ").toUpperCase();
        return "TAD" + uuid.substring(0, 5).replace(" ", "");
    }

    private ItineraryDto toItineraryDto(Itinerary i) {
        ItineraryDto id = new ItineraryDto();
        id.setPnr(i.getPnr());
        id.setUserName(i.getUser().getName());
        id.setEmail(i.getUser().getEmail());
        id.setStatus(i.getStatus());
        id.setTotalAmount(i.getTotalAmount());
        id.setCreatedTime(i.getCreatedTime());

        List<LegDto> legs = new ArrayList<>();
        for (Booking booking : i.getBookings()) {
            // We must fetch flight details to populate the LegDto (Airport codes, times)
            FlightSummaryDto flightDto = flightClient.getFlightById(booking.getFlightId());
            legs.add(toLegDto(booking, flightDto));
        }
        id.setLegs(legs);
        return id;
    }

    private LegDto toLegDto(Booking booking, FlightSummaryDto flightDto) {
        LegDto ld = new LegDto();
        ld.setBookingId(booking.getId());
        ld.setFlightId(booking.getFlightId());
        
        // Populate details from the fetched Flight DTO
        ld.setFromAirport(flightDto.getFromAirport());
        ld.setToAirport(flightDto.getToAirport());
        ld.setDepartureTime(flightDto.getDepartureTime());
        ld.setArrivalTime(flightDto.getArrivalTime());
        
        ld.setSegmentType(booking.getSegmentType());
        ld.setStatus(booking.getStatus());

        List<PassengerDto> passengers = booking.getPassengers().stream().map(this::toPassengerDto).toList();

        ld.setPassengers(passengers);
        return ld;
    }

    private PassengerDto toPassengerDto(Passenger pass) {
        PassengerDto pd = new PassengerDto();
        pd.setName(pass.getName());
        pd.setGender(pass.getGender());
        pd.setAge(pass.getAge());
        pd.setMealType(pass.getMealType());
        pd.setSeatNumber(pass.getSeatNumber());
        return pd;
    }
}