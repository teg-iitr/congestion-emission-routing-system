package com.map.app.model;

import java.util.ArrayList;

import com.graphhopper.util.PointList;
public class RoutePath {
	private ArrayList<Float> lats;
	private ArrayList<Float> longs;
	private ArrayList<String> navigationInstruction;

	public ArrayList<String> getNavigationInstruction() {
		return navigationInstruction;
	}
	public void setNavigationInstruction(ArrayList<String> navigationInstruction) {
		this.navigationInstruction = navigationInstruction;
	}
	public ArrayList<Float> getLats() {
		return lats;
	}
	public void setLats(ArrayList<Float> lats) {
		this.lats = lats;
	}
	public ArrayList<Float> getLongs() {
		return longs;
	}
	public void setLongs(ArrayList<Float> longs) {
		this.longs = longs;
	}
	public RoutePath() {
		lats = new ArrayList<>();
		longs = new ArrayList<>();
	}
	public void fillPath(PointList rp, ArrayList<String> ins) {

		for (int i = 0; i<rp.size(); i++) {
			lats.add((float) rp.getLat(i));
			longs.add((float) rp.getLon(i));
		}
		navigationInstruction = ins;
	}
}