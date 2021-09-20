package com.map.app.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint;
public class RoutePath {
	private ArrayList<GHPoint> points;
	private ArrayList<String> navigationInstruction;
	private ArrayList<Float> bounds;
	private HashMap<String,Float> distNtime;
	
	
	public HashMap<String, Float> getDistNtime() {
		return distNtime;
	}
	public void setDistNtime(HashMap<String, Float> distNtime) {
		this.distNtime = distNtime;
	}
	
	public ArrayList<Float> getBounds() {
		return bounds;
	}
	public void setBounds(ArrayList<Float> bounds) {
		this.bounds = bounds;
	}
	
	public ArrayList<GHPoint> getPoints() {
		return points;
	}
	public void setPoints(ArrayList<GHPoint> points) {
		this.points = points;
	}
	
	public ArrayList<String> getNavigationInstruction() {
		return navigationInstruction;
	}
	public void setNavigationInstruction(ArrayList<String> navigationInstruction) {
		this.navigationInstruction = navigationInstruction;
	}
	public RoutePath() {
		points = new ArrayList<>();
		bounds=new ArrayList<>();
	}
	
	public BBox calcBBox2D(PointList pointList) {
		  BBox bounds = BBox.createInverse(false);
		  for (int i = 0; i < pointList.getSize(); i++) {
		    bounds.update(pointList.getLat(i), pointList.getLon(i));
		  }
		 
		  return bounds;
		}
	public void fillPath(PointList rp, ArrayList<String> ins) {
		
		for (int i = 0; i<rp.size(); i++) {
			points.add(new GHPoint(rp.getLat(i),rp.getLon(i)));
		}
		BBox routeBB=calcBBox2D(rp);
		bounds.add((float) routeBB.minLat);
		bounds.add((float) routeBB.minLon);
		bounds.add((float) routeBB.maxLat);
		bounds.add((float) routeBB.maxLon);
		navigationInstruction = ins;
	}
}