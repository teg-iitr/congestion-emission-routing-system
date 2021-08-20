package com.map.app.dto;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.Profile;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.FlagEncoderFactory;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.PointList;
import com.map.app.model.RouteInformation;
import com.map.app.model.routePath;
public class routePathDto {
	private static final String MAP_URL = "maps/NewDelhi.osm.pbf";
	private String apiKey = "<GH-API-KEY>";
	private GraphHopper gh;
	private Lock readLock;
	private TrafficdatDto dt;
	private InstructionList list;
	public routePathDto(TrafficdatDto dt, GraphHopper hopper, Lock readLock) {
		this.dt = dt;
		this.gh = hopper;
		this.readLock = readLock;
	}

	public routePath find(RouteInformation p) {
		//routing result for given route information
		this.readLock.lock();
		String profile = "";
		//fetching the profile to do routing with
		routePath result = new routePath();
		if (p.getVehicle().equals("car")) {
			if (p.getRouteType().equals("fastest")) {
				profile = "fastest_car";
			} else if (p.getRouteType().equals("balanced")) {
				profile = "balanced_car";
			} else {
				profile = "greenest_car";
			}

		} else if (p.getVehicle().equals("bike")) {
			if (p.getRouteType().equals("fastest")) {
				profile = "fastest_bike";
			} else if (p.getRouteType().equals("balanced")) {
				profile = "balanced_bike";
			} else {
				profile = "greenest_bike";
			}

		} else if (p.getVehicle().equals("foot")) {
			if (p.getRouteType().equals("fastest")) {
				profile = "fastest_foot";
			} else if (p.getRouteType().equals("balanced")) {
				profile = "balanced_foot";
			} else {
				profile = "greenest_foot";
			}

		}
		//making request
		GHRequest request = new GHRequest(p.getStartlat(), p.getStartlon(), p.getEndlat(), p.getEndlon()).setProfile(profile).putHint(Parameters.CH.DISABLE, true);;
		PointList pl = new PointList();
		ArrayList<String> ins = new ArrayList<>();
		try {
			//getting result
			GHResponse fullRes = gh.route(request);
			if (fullRes.hasErrors()) {
				throw new RuntimeException(fullRes.getErrors().toString());
			}
			ResponsePath res = fullRes.getBest();
			System.out.println("Distance in meters: " + res.getDistance());
			System.out.println("Time in minutes: " + res.getTime() / 60000);
			list = res.getInstructions();
			for (Instruction ele: list) {
				if (ele.getSign() != 4) {
					String navIns = ele.getTurnDescription(list.getTr()) + ",covering about " + ele.getDistance() + " meters";
					ins.add(navIns.toLowerCase());
				} else {
					String navIns = ele.getTurnDescription(list.getTr());
					ins.add(navIns);
				}
			}
			pl = res.getPoints();
		} finally {
			result.fillPath(pl, ins);
			readLock.unlock();

		}
		return result; //result contains latitudes and longitudes of route and instructions for navigation

	}

}