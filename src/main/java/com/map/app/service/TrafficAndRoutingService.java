package com.map.app.service;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.graphhopper.json.Statement;

import com.graphhopper.routing.weighting.custom.CustomProfile;
import com.graphhopper.util.CustomModel;
import com.graphhopper.util.shapes.BBox;
import org.springframework.stereotype.Service;
import com.graphhopper.GraphHopper;
import com.graphhopper.GraphHopperConfig;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.ev.UnsignedDecimalEncodedValue;
//import com.graphhopper.matching.*;
import com.map.app.containers.AirQualityDataExtractor;
import com.map.app.containers.TrafficDataExtractor;
import com.map.app.containers.RoutePathContainer;
import com.map.app.graphhopperfuncs.MyGraphHopper;
import com.map.app.model.UrlContainer;
import com.map.app.model.RoutePath;
import com.map.app.model.TrafficData;

/**
 * @author Siftee, Amit
 */

@Service
public class TrafficAndRoutingService {

	private String apiKey = System.getenv("here_api_key");
	
	private final AirQualityDataExtractor airQualityDataExtractor;
	private final TrafficDataExtractor trafficDataExtractor;
	private final RoutePathContainer routePathContainer;
	private final BBox boundingBox;
	// a few settings for here maps real-time congestion data
	public enum SpeedChoice{avg_actual_from_hereMaps, free_flow_from_hereMaps, lower_of_two}
	public static SpeedChoice speedChoice = SpeedChoice.lower_of_two;
	public static float functional_road_class_here_maps = 4.0f;

	public RoutePathContainer getRoutePathContainer() {
		return routePathContainer;
	}

	public TrafficAndRoutingService() {
		ReadWriteLock lock=new ReentrantReadWriteLock();
    	GraphHopperConfig config=new GraphHopperConfig();
    	config.putObject("index.max_region_search", 8); // increasing the search radius (a point in Rajaji forest is not able to find any road)
    	GraphHopper gh=new MyGraphHopper();
		int defaultSmoke;
		int defaultTime;
		boolean turnCosts;
		Properties prop=new Properties();
		try (FileInputStream ip = new FileInputStream("config.properties")) {
			prop.load(ip);
			defaultSmoke = Integer.parseInt(prop.getProperty("default_smoke"));
			defaultTime = Integer.parseInt(prop.getProperty("default_time"));
			turnCosts = Boolean.parseBoolean(prop.getProperty("turn_costs"));
		} catch (IOException e) {
			throw new RuntimeException("Config properties are not found. Aborting ...");
		}
		UnsignedDecimalEncodedValue smokeEnc=new UnsignedDecimalEncodedValue("smoke",31,0.1, defaultSmoke,true);
		UnsignedDecimalEncodedValue timeEnc=new UnsignedDecimalEncodedValue("time",31,0.1, defaultTime,true);
		gh.getEncodingManagerBuilder().add(smokeEnc);
		gh.getEncodingManagerBuilder().add(timeEnc);
    	//gh.c
		try (FileInputStream ip = new FileInputStream("config.properties")) {
			prop.load(ip);
			System.out.println("Using OSM file "+ prop.getProperty("datareader.file"));
			config.putObject("datareader.file",prop.getProperty("datareader.file"));
			List<Profile> profiles = new ArrayList<>();

			for (PathChoice pc : PathChoice.values()) {
				for (TransportMode tm : TransportMode.values()) {
					if(!pc.toString().equals("all"))
						if (tm.toString().equals("foot"))
							profiles.add(new Profile(TrafficAndRoutingService.getModeBasedPathChoice(pc, tm)).setVehicle(tm.toString()).setWeighting(pc.toString()));
						else
							profiles.add(new Profile(TrafficAndRoutingService.getModeBasedPathChoice(pc, tm)).setVehicle(tm.toString()).setTurnCosts(turnCosts).setWeighting(pc.toString()));
				}
			}

			profiles.add(new Profile("ipt").setVehicle("car").setTurnCosts(turnCosts).setWeighting("fastest"));

			// see https://github.com/graphhopper/graphhopper/blob/4.x/docs/core/custom-models.md
			CustomModel bus_custom_model = new CustomModel();
			bus_custom_model.addToPriority(Statement.If( "road_class == RESIDENTIAL", Statement.Op.LIMIT, 0.1));
			profiles.add(new CustomProfile("bus").setCustomModel(bus_custom_model).setVehicle("car").setTurnCosts(turnCosts));

			CustomModel metro_custom_model = new CustomModel();
			metro_custom_model.addToPriority(Statement.If( "road_class != TRUNK", Statement.Op.LIMIT, 0.1));
			profiles.add(new CustomProfile("metro").setCustomModel(metro_custom_model).setVehicle("car").setTurnCosts(turnCosts));
			config.setProfiles(profiles);
			config.putObject("graph.flag_encoders",prop.getProperty("graph.flag_encoders"));
			config.putObject("graph.dataaccess", prop.getProperty("graph.dataaccess"));
			config.putObject("profiles_ch", prop.getProperty("profiles_ch"));
			
			if(apiKey==null)
				apiKey = prop.getProperty("here_api_key"); // the api key must be in either system env or config.properties
		} catch (IOException e) {
			throw new RuntimeException("Config properties are not found. Aborting ...");
		}

    	gh.init(config).setGraphHopperLocation("graphLocation");
    	//System.out.println(gh.getEncodingManager().getDecimalEncodedValue("smoke"));
    	gh.clean();   	
    	gh.importOrLoad();
    	//gh.set
    	this.boundingBox = gh.getGraphHopperStorage().getBaseGraph().getBounds();
    	trafficDataExtractor = new TrafficDataExtractor(gh,lock.writeLock());
    	routePathContainer = new RoutePathContainer(gh, lock.readLock());
    	airQualityDataExtractor = new AirQualityDataExtractor(gh,lock.writeLock());
    }
	
	public static String getModeBasedPathChoice(PathChoice pathChoice, TransportMode transportMode) {
		return pathChoice.toString().concat("_").concat(transportMode.toString());
	}
	
	public ArrayList<Float> getBoundingBox() {
		ArrayList<Float> box=new ArrayList<>();

		box.add((float)boundingBox.minLat);
		box.add((float)boundingBox.minLon);
		box.add((float)boundingBox.maxLat);
		box.add((float)boundingBox.maxLon);
		
		return box;
	}
    public TrafficData getAll()
	{
		return trafficDataExtractor.getRoads();
	}
	
	public void start() {
		trafficDataExtractor.readHEREMapData(apiKey, this.boundingBox);
		
		airQualityDataExtractor.readWAQIData(this.boundingBox);
		
	}

	public ArrayList<RoutePath> getPath(UrlContainer p) {
		return routePathContainer.find(p);
	}

}
