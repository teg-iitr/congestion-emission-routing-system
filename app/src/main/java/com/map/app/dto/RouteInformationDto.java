package com.map.app.dto;

import com.map.app.model.RouteInformation;

public class RouteInformationDto {
	private String StartLoc;
	private String EndLoc;
	private String RouteType;
	private String Vehicle;
	//getters and setters
	public String getStartLoc() {
		return StartLoc;
	}
	public void setStartLoc(String startLoc) {
		StartLoc = startLoc;
	}
	public String getEndLoc() {
		return EndLoc;
	}
	public void setEndLoc(String endLoc) {
		EndLoc = endLoc;
	}
	public String getRouteType() {
		return RouteType;
	}
	public void setRouteType(String routeType) {
		RouteType = routeType;
	}
	public String getVehicle() {
		return Vehicle;
	}
	public void setVehicle(String vehicle) {
		Vehicle = vehicle;
	}
	public RouteInformation conv() {
		//converting dto layer to model layer
		RouteInformation rp = new RouteInformation();
		String[] startCoords = this.getStartLoc().split(",");
		String[] endCoords = this.getEndLoc().split(",");
		rp.setStartlat(Float.parseFloat(startCoords[1]));
		rp.setStartlon(Float.parseFloat(startCoords[0]));
		rp.setEndlat(Float.parseFloat(endCoords[1]));
		rp.setEndlon(Float.parseFloat(startCoords[0]));
		rp.setRouteType(this.getRouteType());
		rp.setVehicle(this.getVehicle());
		return rp;
	}
	@Override
	public String toString() {
		return "RouteInformationDto [StartLoc=" + StartLoc + ", EndLoc=" + EndLoc + ", RouteType=" + RouteType +
			", Vehicle=" + Vehicle + "]";
	}

}