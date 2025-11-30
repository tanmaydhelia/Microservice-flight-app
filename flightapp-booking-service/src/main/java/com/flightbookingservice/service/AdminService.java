package com.flightapp.service;

import com.flightapp.dto.AirlineInventoryRequest;
import com.flightapp.dto.AirlineInventoryResponse;

public interface AdminService {
	AirlineInventoryResponse addInventory (AirlineInventoryRequest request);
}
