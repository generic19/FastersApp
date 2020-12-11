package com.basilalasadi.fasters.model;

import com.jsoniter.JsonIterator;
import com.jsoniter.output.JsonStream;

import java.io.IOException;


public class LocationModel {
	
	public final String country;
	public final String admin;
	public final String city;
	public final double longitude;
	public final double latitude;
	
	public LocationModel(String country, String admin, String city, double longitude, double latitude) {
		this.country = country;
		this.admin = admin;
		this.city = city;
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	public LocationViewModel toViewModel() {
		return new LocationViewModel(country, String.format("%s, %s", city, admin));
	}
	
	public String serialize() {
		return JsonStream.serialize(this);
	}
	
	public static LocationModel deserialize(String input) throws IOException {
		return JsonIterator.deserialize(input, LocationModel.class);
	}
}
