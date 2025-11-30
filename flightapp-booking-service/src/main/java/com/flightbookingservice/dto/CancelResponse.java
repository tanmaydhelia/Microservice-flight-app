package com.flightbookingservice.dto;

import com.flightbookingservice.entity.BookingStatus;

public class CancelResponse {
	private String pnr;
	private BookingStatus status;
	private String message;
	public String getPnr() {
		return pnr;
	}
	public void setPnr(String pnr) {
		this.pnr = pnr;
	}
	public BookingStatus getStatus() {
		return status;
	}
	public void setStatus(BookingStatus status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
