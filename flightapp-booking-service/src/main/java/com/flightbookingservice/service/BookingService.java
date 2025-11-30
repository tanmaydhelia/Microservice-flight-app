package com.flightapp.service;

import java.util.List;

import com.flightapp.dto.BookingRequest;
import com.flightapp.dto.CancelResponse;
import com.flightapp.dto.ItineraryDto;

public interface BookingService {
	
	ItineraryDto bookItinerary(int outwardFlightId, BookingRequest req);
	
	ItineraryDto getItineraryByPnr(String pnr);
	
	List<ItineraryDto> getHistoryByEmail(String email);
	
	CancelResponse cancelByPnr(String pnr);
}
