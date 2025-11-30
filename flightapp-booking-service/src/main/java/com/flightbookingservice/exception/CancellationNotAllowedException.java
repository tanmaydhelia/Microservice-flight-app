package com.flightapp.exception;

public class CancellationNotAllowedException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public CancellationNotAllowedException(String msg) {
		super(msg);
	}
}
