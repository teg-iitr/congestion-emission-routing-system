package com.map.app.containers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.PointList;
import com.map.app.model.UrlContainer;
import com.map.app.model.RoutePath;
import com.map.app.service.PathChoice;
import com.map.app.service.TrafficAndRoutingService;
import com.map.app.service.TransportMode;

/**
 * @author Siftee, Amit
 */
public class RoutePathContainer {
	private final GraphHopper gh;
	private final Lock readLock;

	public RoutePathContainer(GraphHopper hopper, Lock readLock) {
		this.gh = hopper;
		this.readLock = readLock;
	}

	public RoutePath find(UrlContainer p) {
		//routing result for given route information
		this.readLock.lock();

		//fetching the profile to do routing with
		String profile;
		switch (p.getVehicle()) {
			case "bus":
				profile = "bus";
				break;
			case "ipt":
				profile = "ipt";
				break;
			case "metro":
				profile = "metro";
				break;
			default:
				TransportMode mode = TransportMode.valueOf(p.getVehicle());
				PathChoice pathChoice = PathChoice.valueOf(p.getRouteType());
				profile = TrafficAndRoutingService.getModeBasedPathChoice(pathChoice, mode);
				break;
		}

		RoutePath result = new RoutePath();
		//making request
		GHRequest request = new GHRequest(p.getStartlat(), p.getStartlon(), p.getEndlat(), p.getEndlon()).setProfile(profile).putHint(Parameters.CH.DISABLE, true);;
		PointList pl = new PointList();
		HashMap<String,Float> map=new HashMap<>();
		ArrayList<String> ins = new ArrayList<>();
		try {
			//getting result
			GHResponse fullRes = gh.route(request);
			if (fullRes.hasErrors()) {
				throw new RuntimeException(fullRes.getErrors().toString());
			}
			ResponsePath res = fullRes.getBest();

			map.put("distance", (float)res.getDistance()); // m.
			System.out.println("Distance in meters: " + res.getDistance());
			map.put("time", (float)(res.getTime() / (1000.))); // sec.
			System.out.println("Time in minutes: " + res.getTime() / (60.*1000.));
			
			InstructionList list = res.getInstructions();
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
			result.setSummary(map);
			readLock.unlock();
		}
		return result; //result contains latitudes and longitudes of route and instructions for navigation

	}

}