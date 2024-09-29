package com.map.app.model;

/**
 * @author Siftee
 */
public class UrlContainer {
	private double Startlat;
	private double Startlon;
	private double Endlat;
	private double Endlon;
	private String Vehicle;
	private String RouteType;
	
	
	
	public double getStartlat() {
		return Startlat;
	}
	public void setStartlat(float startlat) {
		Startlat = startlat;
	}
	public double getStartlon() {
		return Startlon;
	}
	public void setStartlon(float startlon) {
		Startlon = startlon;
	}
	public String getVehicle() {
		return Vehicle;
	}
	public void setVehicle(String vehicle) {
		Vehicle = vehicle;
	}
	public double getEndlat() {
		return Endlat;
	}
	public void setEndlat(float endlat) {
		Endlat = endlat;
	}
	public double getEndlon() {
		return Endlon;
	}
	public void setEndlon(float endlon) {
		Endlon = endlon;
	}
	public String getRouteType() {
		return RouteType;
	}
	public void setRouteType(String routeType) {
		RouteType = routeType;
	}
	public String toString() {
		return Startlat + " " + Startlon + " " + Endlat + " " + Endlon + " " + Vehicle + " " + RouteType;
	}

}