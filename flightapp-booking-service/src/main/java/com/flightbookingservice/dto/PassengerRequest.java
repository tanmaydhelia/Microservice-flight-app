package com.flightbookingservice.dto;

import org.hibernate.validator.constraints.Range;

import com.flightbookingservice.entity.Gender;
import com.flightbookingservice.entity.MealType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PassengerRequest {

	@NotBlank
	private String name;
	
	@NotNull
	private Gender gender;
	
	@NotNull
	@Range(min=0,max=100)
	private int age;
	
	@NotNull
	private MealType mealType;
	
	@NotBlank
	private String seatNumber;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public MealType getMealType() {
		return mealType;
	}

	public void setMealType(MealType mealType) {
		this.mealType = mealType;
	}

	public String getSeatNumber() {
		return seatNumber;
	}

	public void setSeatNumber(String seatNumber) {
		this.seatNumber = seatNumber;
	}
}
