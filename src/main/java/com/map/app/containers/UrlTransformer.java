package com.map.app.containers;

import com.map.app.model.UrlContainer;

/**
 * @author Siftee
 */
public class UrlTransformer {
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

	public UrlContainer convert() {
		//converting dto layer to model layer
		UrlContainer rp = new UrlContainer();
		String[] startCoords = this.getStartLoc().split(",");
		String[] endCoords = this.getEndLoc().split(",");
		rp.setStartlat(Float.parseFloat(startCoords[1]));
		rp.setStartlon(Float.parseFloat(startCoords[0]));
		rp.setEndlat(Float.parseFloat(endCoords[1]));
		rp.setEndlon(Float.parseFloat(endCoords[0]));
		rp.setRouteType(this.getRouteType());
		rp.setVehicle(this.getVehicle());
		return rp;
	}

	@Override
	public String toString() {
		return "RouteInformationDto [ StartLoc=" + StartLoc + ", EndLoc=" + EndLoc + ", RouteType=" + RouteType +
			", Vehicle=" + Vehicle + "]";
	}

}