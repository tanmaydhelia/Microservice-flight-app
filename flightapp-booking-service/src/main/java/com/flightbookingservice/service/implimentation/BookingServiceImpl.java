package com.flightapp.service.implimentation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flightapp.dto.BookingRequest;
import com.flightapp.dto.CancelResponse;
import com.flightapp.dto.ItineraryDto;
import com.flightapp.dto.LegDto;
import com.flightapp.dto.PassengerDto;
import com.flightapp.dto.PassengerRequest;
import com.flightapp.dto.TripType;
import com.flightapp.entity.Booking;
import com.flightapp.entity.BookingStatus;
import com.flightapp.entity.Flight;
import com.flightapp.entity.Itinerary;
import com.flightapp.entity.Passenger;
import com.flightapp.entity.Role;
import com.flightapp.entity.TripSegmentType;
import com.flightapp.entity.User;
import com.flightapp.exception.CancellationNotAllowedException;
import com.flightapp.exception.ResourceNotFoundException;
import com.flightapp.exception.SeatNotAvailableException;
import com.flightapp.repository.FlightRepository;
import com.flightapp.repository.ItineraryRepository;
import com.flightapp.repository.UserRepository;
import com.flightapp.service.BookingService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService{

	private final UserRepository userRepository;
	private final FlightRepository flightRepository;
	private final ItineraryRepository itineraryRepository;
	
	public BookingServiceImpl(UserRepository userRepository, FlightRepository flightRepository,
			ItineraryRepository itineraryRepository) {
		super();
		this.userRepository = userRepository;
		this.flightRepository = flightRepository;
		this.itineraryRepository = itineraryRepository;
	}

	@Override
	@Transactional
	public ItineraryDto bookItinerary(int outwardFlightId, BookingRequest req) {
		log.info("Booking initireary for email={} tripType={} outwardFlight={}", req.getEmail(), req.getTripType(), outwardFlightId);
		
		validateBookingReq(req);
		
		User user = getOrCreateUser(req.getName(), req.getEmail());
		
		Flight outwardFlight = getFlightOrThrow(outwardFlightId);
		Flight returnFlight = null;
		boolean isRoundTrip = req.getTripType()==TripType.ROUND_TRIP;
		
		if(isRoundTrip) {
			if(req.getReturnFlightId()==null) {
				throw new IllegalArgumentException("Return FlightId required!!!");
			}
			returnFlight = getFlightOrThrow(req.getReturnFlightId());
		}
		
		int seats = req.getNumberOfSeats();
		// Seat availability validation
		if(outwardFlight.getAvailableSeats() < seats) {
			throw new SeatNotAvailableException("Not enough seats available on outward flight");
		}
		if(isRoundTrip && returnFlight.getAvailableSeats() < seats) {
			throw new SeatNotAvailableException("Not enough seats available on return flight");
		}
		// Duplicate seat numbers within request validation
		Set<String> seatNumbers = new HashSet<>();
		for(PassengerRequest pr: req.getPassengers()) {
			if(!seatNumbers.add(pr.getSeatNumber())) {
				throw new SeatNotAvailableException("Duplicate seat number in request: "+pr.getSeatNumber());
			}
		}
		
		int outwardAmount = outwardFlight.getPrice()*seats;
		int returnAmount = isRoundTrip?(returnFlight.getPrice()*seats):0;
		
		int totalAmount = outwardAmount+returnAmount;
		
		int updatedSeats = outwardFlight.getAvailableSeats() - seats;
		outwardFlight.setAvailableSeats(updatedSeats);
		flightRepository.save(outwardFlight);
		if(isRoundTrip) {
			returnFlight.setAvailableSeats(returnFlight.getAvailableSeats() - seats);
			flightRepository.save(returnFlight);
		}
		
		Itinerary i = new Itinerary();
		i.setPnr(generatePnr());
		i.setUser(user);
		i.setCreatedTime(LocalDateTime.now());
		i.setTotalAmount(totalAmount);
		i.setStatus(BookingStatus.BOOKED);
		
		List<Booking> bookings = new ArrayList<>();
		
		Booking outwardBooking = createBookingLeg(i,outwardFlight, req, isRoundTrip?TripSegmentType.OUTBOUND:TripSegmentType.ONE_WAY);
		bookings.add(outwardBooking);
		if(isRoundTrip && returnFlight!=null) {
			Booking returnBooking = createBookingLeg(i,returnFlight, req, TripSegmentType.RETURN);
			bookings.add(returnBooking);
		}
		i.setBookings(bookings);
		// Cascade will persist bookings & passengers with itinerary
		itineraryRepository.save(i);
		
		log.info("Itinerary added successfully with PNR={} for user={}", i.getPnr(), user.getName());
		
		return toItineraryDto(i);
	}

	private void validateBookingReq(BookingRequest req) {
		if(req.getPassengers()==null || req.getPassengers().isEmpty()) {
			throw new IllegalArgumentException("Add Atleast one passenger!!!");
		}
		if(req.getNumberOfSeats()==0) {
			throw new IllegalArgumentException("No Seats requested!!!");
		}
		if(req.getPassengers().size()!=req.getNumberOfSeats()) {
			throw new IllegalArgumentException("Number of seats != number of passengers!!!");
		}
	}
	private User getOrCreateUser(String name, String email) {
		return userRepository.findByEmail(email).orElseGet(()->{
			log.info("User Not found for email={}, hence CREATING NEW USER", email);
			User u = new User();
			u.setName(name);
			u.setEmail(email);
			u.setRole(Role.USER);
			return userRepository.save(u);
		});
	}
	private Flight getFlightOrThrow(int outwardFlightId) {
		return flightRepository.findById(outwardFlightId).orElseThrow(()-> new ResourceNotFoundException("Flight not found for id="+outwardFlightId));
	}

	@Override
	@Transactional(readOnly = true)
	public ItineraryDto getItineraryByPnr(String pnr) {
		log.info("Fetching itinerary by PNR={}",pnr);
		Itinerary i = itineraryRepository.findByPnr(pnr).orElseThrow(()-> new ResourceNotFoundException("No itinerary for pnr!!!"));
		return toItineraryDto(i);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ItineraryDto> getHistoryByEmail(String email) {
		log.info("Fetching booking history for email={}",email);
		List<Itinerary> i = itineraryRepository.findByUserEmail(email);
			
		log.debug("Found {} itineraries", i.size());
		
		return i.stream().map(this::toItineraryDto).toList();
	}

	@Override
	@Transactional
	public CancelResponse cancelByPnr(String pnr) {
		log.info("Attempting Cancel for pnr = {}", pnr);
		
		Itinerary i = itineraryRepository.findByPnr(pnr).orElseThrow(()-> new ResourceNotFoundException("Itinerary not found for PNR: "+pnr));
		
		LocalDateTime curr = LocalDateTime.now();
		
		for(Booking b : i.getBookings()) {
			if(b.getStatus() != BookingStatus.BOOKED) {
				continue;
			}
			
			LocalDateTime deptTime = b.getFlight().getDepartureTime();
			if(curr.plusHours(24).isAfter(deptTime)) {
				log.warn("Cancellation not Allowed for pnr={}",pnr);
				throw new CancellationNotAllowedException("Cannot Cancel as booking within 24hrs");
			}
		}
		
		for(Booking b: i.getBookings()) {
			if(b.getStatus()==BookingStatus.BOOKED) {
				b.setStatus(BookingStatus.CANCELLED);
				int passengerCount = b.getPassengers().size();
				Flight f = b.getFlight();
				f.setAvailableSeats(f.getAvailableSeats()+passengerCount);
				flightRepository.save(f);
			}
		}
		
		i.setStatus(BookingStatus.CANCELLED);
		itineraryRepository.save(i);
		
		log.info("Cancellation Successful for pnr={}!!!",pnr);
		
		CancelResponse cr = new CancelResponse();
		cr.setPnr(pnr);
		cr.setStatus(BookingStatus.CANCELLED);
		cr.setMessage("Booking Cancelled Successfully!!!");
		return cr;
	}
	
	private Booking createBookingLeg(Itinerary it, Flight f, BookingRequest req, TripSegmentType segmentType) {
		Booking booking = new Booking();
		booking.setItinerary(it);
		booking.setFlight(f);
		booking.setJourneyDate(f.getDepartureTime().toLocalDate());
		booking.setSegmentType(segmentType);
		booking.setStatus(BookingStatus.BOOKED);
		
		List<Passenger> passengers = new ArrayList<>();
		for(PassengerRequest pr: req.getPassengers()) {
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
		String uuid = UUID.randomUUID().toString().replace("-"," ").toUpperCase();
		return "TAD"+uuid.substring(0,5).replace(" ","");
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
		for(Booking booking: i.getBookings()) {
			legs.add(toLegDto(booking));
		}
		id.setLegs(legs);
		return id;
	}
	
	private LegDto toLegDto(Booking booking) {
		LegDto ld = new LegDto();
		ld.setBookingId(booking.getId());
		ld.setFlightId(booking.getFlight().getId());
		ld.setFromAirport(booking.getFlight().getFromAirport());
		ld.setToAirport(booking.getFlight().getToAirport());
		ld.setDepartureTime(booking.getFlight().getDepartureTime());
		ld.setArrivalTime(booking.getFlight().getArrivalTime());
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
