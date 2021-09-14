package com.map.app.model;

import com.map.app.dto.RouteInformationDto;
public class RouteInformation {
	private float Startlat;
	private float Startlon;
	private float Endlat;
	private float Endlon;
	private String Vehicle;
	public String getVehicle() {
		return Vehicle;
	}
	public void setVehicle(String vehicle) {
		Vehicle = vehicle;
	}
	public String getRouteType() {
		return RouteType;
	}
	public void setRouteType(String routeType) {
		RouteType = routeType;
	}
	private String RouteType;
	public float getStartlat() {
		return Startlat;
	}
	public void setStartlat(float startlat) {
		Startlat = startlat;
	}
	public float getStartlon() {
		return Startlon;
	}
	public void setStartlon(float startlon) {
		Startlon = startlon;
	}
	public float getEndlat() {
		return Endlat;
	}
	public void setEndlat(float endlat) {
		Endlat = endlat;
	}
	public float getEndlon() {
		return Endlon;
	}
	public void setEndlon(float endlon) {
		Endlon = endlon;
	}
	public String toString() {
		return Startlat + " " + Startlon + " " + Endlat + " " + Endlon + " " + Vehicle + " " + RouteType;
	}

}