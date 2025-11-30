package com.flightapp.flightservice.service;

import com.flightapp.flightservice.dto.request.AirlineInventoryRequest;
import com.flightapp.flightservice.dto.response.AirlineInventoryResponse;

public interface AdminService {
	AirlineInventoryResponse addInventory (AirlineInventoryRequest request);
}
