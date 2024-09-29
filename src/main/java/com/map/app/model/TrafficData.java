package com.map.app.model;
import java.util.List;

/**
 * @author Siftee
 */
public class TrafficData {

	private List<List<Float>> lat;
	private List<List<Float>> lons;
	private List<List<Float>> speed;
	private List<List<Float>> time;
	public List<List<Float>> getLat() {
		return lat;
	}
	public void setLat(List<List<Float>> lat) {
		this.lat = lat;
	}
	public List<List<Float>> getLons() {
		return lons;
	}
	public void setLons(List<List<Float>> lons) {
		this.lons = lons;
	}
	public List<List<Float>> getSpeed() {
		return speed;
	}
	public void setSpeed(List<List<Float>> speed) {
		this.speed = speed;
	}
	public List<List<Float>> getTime() {
		return time;
	}
	public void setTime(List<List<Float>> time) {
		this.time = time;
	}
}