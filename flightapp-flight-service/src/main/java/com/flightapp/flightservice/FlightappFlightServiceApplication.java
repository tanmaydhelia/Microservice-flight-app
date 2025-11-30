package com.flightapp.flightservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class FlightappFlightServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlightappFlightServiceApplication.class, args);
	}

}
