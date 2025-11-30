package com.flightbookingservice.service;

import java.util.List;

import com.flightbookingservice.dto.BookingRequest;
import com.flightbookingservice.dto.CancelResponse;
import com.flightbookingservice.dto.ItineraryDto;

public interface BookingService {
	
	ItineraryDto bookItinerary(int outwardFlightId, BookingRequest req);
	
	ItineraryDto getItineraryByPnr(String pnr);
	
	List<ItineraryDto> getHistoryByEmail(String email);
	
	CancelResponse cancelByPnr(String pnr);
}
