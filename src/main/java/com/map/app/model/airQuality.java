package com.map.app.model;

public class airQuality {
	private double lat;
	private double lon;
	private double aqi;
	private String stationName;
	public airQuality(double lat, double lon, double aqi, String name) {
		this.lat = lat;
		this.lon = lon;
		this.aqi = aqi;
		this.stationName = name;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public double getAqi() {
		return aqi;
	}

	public void setAqi(double aqi) {
		this.aqi = aqi;
	}

	public String getStationName() {
		return stationName;
	}

	public void setStationName(String stationName) {
		
		this.stationName = stationName;
	}

	public String toString() {
		return this.lat + " " + this.lon + " " + this.aqi + " " + this.stationName;
	}

}
