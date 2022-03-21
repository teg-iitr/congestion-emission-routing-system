package com.map.app.containers;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
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
import com.map.app.graphhopperfuncs.concentrationCalc;
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
		initializeResultsCSV();
	}
	
	public RoutePath finalPath(UrlContainer p,String profile,TransportMode mode)
	{
		RoutePath indiv=new RoutePath();
		// set routing algorithm
		GHRequest request=new GHRequest(p.getStartlat(), p.getStartlon(), p.getEndlat(), p.getEndlon()).setProfile(profile).putHint(Parameters.CH.DISABLE, true);
		request.setPathDetails(List.of(
				Parameters.Details.EDGE_ID
		));
//		request.setAlgorithm(Parameters.Algorithms.ASTAR_BI);
//		request.setAlgorithm(Parameters.Algorithms.ALT_ROUTE);
		PointList pl = new PointList();
		HashMap<String,Float> map=new HashMap<>();
		ArrayList<String> ins = new ArrayList<>();
		try 
		{//getting result
			GHResponse fullRes = gh.route(request);
			if (fullRes.hasErrors()) 
			{
				throw new RuntimeException(fullRes.getErrors().toString());
				}
			ResponsePath res = fullRes.getBest();
			//System.out.println(res.getLegs().size());
			//System.out.println(profile);
			double concScore=(concentrationCalc.calcConcentrationScore(gh,res.getPathDetails().get(Parameters.Details.EDGE_ID),mode));
			map.put("distance", (float)res.getDistance()); // m.
						//System.out.println("Distance in meters: " + res.getDistance());
			
			map.put("time", (float)((res.getTime()/(60))/1000)); // sec.
						//System.out.println("Time in minutes: " + (res.getTime()/(60))/1000);
			map.put("mean aqi",(float) (concScore)); // sec.
			map.put("exposure", (float) (concScore*((res.getTime()/(60))/1000)));
			InstructionList list = res.getInstructions();
			for (Instruction ele: list) {
				if (ele.getSign() != 4) {
					String navIns = ele.getTurnDescription(list.getTr()) + ",covering about " + ele.getDistance() + " meters";
					ins.add(navIns.toLowerCase());
					} 
				else {
					String navIns = ele.getTurnDescription(list.getTr());
					ins.add(navIns);
					}
				}

			ins.add("DISTANCE IN METERS: "+res.getDistance());
			ins.add("TIME IN MINUTES: "+((res.getTime()/(60))/1000));
			ins.add("SUM OF CONCENTRATION SCORE: "+concScore);
			ins.add("TOTAL EXPOSURE: "+(concScore*((res.getTime()/(60))/1000)));
//			writeResults(fullRes.);
			String origin_lat = String.valueOf(request.getPoints().get(0).lat);
			String origin_lon = String.valueOf(request.getPoints().get(0).lon);
			String destination_lat = String.valueOf(request.getPoints().get(request.getPoints().size() -1).lat);
			String destination_lon = String.valueOf(request.getPoints().get(request.getPoints().size() -1).lon);
			writeResults(
					origin_lat,
					origin_lon,
					destination_lat,
					destination_lon,
					profile.split("_")[0],
					res.getDistance(),
					((res.getTime()/(60))/1000),
					concScore,
					(concScore*((res.getTime()/(60))/1000)));
			pl = res.getPoints();
			} 
		finally {
			indiv.fillPath(pl, ins);
			indiv.setSummary(map);
			}
		return indiv;
	}

	public ArrayList<RoutePath> find(UrlContainer p) {
		//routing result for given route information
		this.readLock.lock();
		ArrayList<RoutePath> result = new ArrayList<>();
		
		try
		{
		//fetching the profile to do routing with
		String profile="";
		TransportMode mode=TransportMode.valueOf("car");
		PathChoice pathChoice;
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
				 mode= TransportMode.valueOf(p.getVehicle());
				 pathChoice= PathChoice.valueOf(p.getRouteType());
				 if(!pathChoice.toString().equals("all"))
					profile = TrafficAndRoutingService.getModeBasedPathChoice(pathChoice, mode);
				break;
		}

		if(profile.length()!=0)
		{
			result.add(finalPath(p,profile,mode));
		}
		else 
		{
			for(PathChoice pc:PathChoice.values())
			{
				if(!pc.toString().equals("all"))
				{
					
					profile = TrafficAndRoutingService.getModeBasedPathChoice(pc, mode);
					result.add(finalPath(p,profile,mode));
				}
			}
		}
		}
		finally
		{
		readLock.unlock();
		}
		return result; //result contains latitudes and longitudes of route and instructions for navigation
	}
	public static void initializeResultsCSV() {
		FileWriter csvwriter;	
		BufferedWriter bufferedWriter = null;
			try {
				Properties prop=new Properties();
				String outputDir;
				try (FileInputStream ip = new FileInputStream("config.properties")) {
					prop.load(ip);
					outputDir = prop.getProperty("output_results");
				} catch (IOException e) {
					throw new RuntimeException("Config properties are not found. Aborting ...");
				}

				csvwriter = new FileWriter(outputDir, false);
				bufferedWriter = new BufferedWriter(csvwriter);
				StringJoiner stringJoiner = new StringJoiner(",");
				stringJoiner
						.add("origin_lat")
						.add("origin_lon")
						.add("destination_lat")
						.add("destination_lon")
						.add("routing")
						.add("distance")
						.add("time")
						.add("concentration")
						.add("exposure");
				bufferedWriter.write(stringJoiner.toString());
				bufferedWriter.newLine();	}
			catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {assert bufferedWriter != null;
				bufferedWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
				}
				try {
					bufferedWriter.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
	}
	public static void writeResults(String origin_lat,  String origin_lon, String destination_lat, String destination_lon, String routing, double dist, double time, double conc, double exposure) {
		FileWriter csvwriter;
		BufferedWriter bufferedWriter = null;
		try {
			Properties prop=new Properties();
			String outputDir;
			try (FileInputStream ip = new FileInputStream("config.properties")) {
				prop.load(ip);
				outputDir = prop.getProperty("output_results");
			} catch (IOException e) {
				throw new RuntimeException("Config properties are not found. Aborting ...");
			}
			csvwriter = new FileWriter(outputDir, true);
			bufferedWriter = new BufferedWriter(csvwriter);
			StringJoiner stringJoiner = new StringJoiner(",");
			stringJoiner
					.add(origin_lat)
					.add(origin_lon)
					.add(destination_lat)
					.add(destination_lon)
					.add(routing)
					.add(String.valueOf(dist))
					.add(String.valueOf(time))
					.add(String.valueOf(conc))
					.add(String.valueOf(exposure));
			bufferedWriter.write(stringJoiner.toString());
			bufferedWriter.newLine();	}
		catch (IOException e) {
			e.printStackTrace();
		} finally {
			try { assert bufferedWriter != null;
				bufferedWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				bufferedWriter.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

}