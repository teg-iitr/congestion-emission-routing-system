package com.map.app.service;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.graphhopper.util.shapes.BBox;
import org.springframework.stereotype.Service;
import com.graphhopper.GraphHopper;
import com.graphhopper.GraphHopperConfig;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.ev.UnsignedDecimalEncodedValue;
import com.map.app.dto.AirQualityDto;
import com.map.app.dto.TrafficdatDto;
import com.map.app.dto.RoutePathDto;
import com.map.app.graphhopperfuncs.MyGraphHopper;
import com.map.app.model.RouteInformation;
import com.map.app.model.RoutePath;
import com.map.app.model.Trafficdat;

/**
 * @author Siftee, Amit
 */

@Service
public class TrafficAndRoutingService {
	
	private final ReadWriteLock lock;
	
	private final GraphHopper gh;
	private static final String MAP_URL="maps/planet_77.734,29.841_78.327,30.369.osm.pbf";
	private final String apiKey=System.getenv("here_api_key");
	private final AirQualityDto ai;
	private final TrafficdatDto dt;
	private final RoutePathDto rp;
	private final BBox boundingBox;
	
    public TrafficAndRoutingService()
    {
    	lock=new ReentrantReadWriteLock();
    	GraphHopperConfig args=new GraphHopperConfig();
    	UnsignedDecimalEncodedValue smokeEnc=new UnsignedDecimalEncodedValue("smoke",16,0.1,0,true); //maxValue->155.5
    	
    	gh=new MyGraphHopper();
    	//gh.getEncodingManager().
    	//gh.createWeighting(null, null, false)
    	gh.getEncodingManagerBuilder().add(smokeEnc);
    	//gh.c
    	Properties prop=new Properties();
		try(FileInputStream ip = new FileInputStream("config.properties");) {
			prop.load(ip);
			System.out.println("Using OSM file "+ MAP_URL);
			args.putObject("datareader.file",MAP_URL);
			List<Profile> profiles = new ArrayList<>();
			// I think, we can use enums in the followings, Amit Sep'21.
			profiles.add(new Profile("balanced_car").setVehicle("car").setWeighting("balanced"));
			profiles.add(new Profile("fastest_car").setVehicle("car").setWeighting("fastest"));
			profiles.add(new Profile("greenest_car").setVehicle("car").setWeighting("greenest"));
			profiles.add(new Profile("greenest_bike").setVehicle("bike").setWeighting("greenest"));
			profiles.add(new Profile("fastest_bike").setVehicle("bike").setWeighting("fastest"));
			profiles.add(new Profile("balanced_bike").setVehicle("bike").setWeighting("balanced"));
			profiles.add(new Profile("greenest_foot").setVehicle("foot").setWeighting("greenest"));
			profiles.add(new Profile("fastest_foot").setVehicle("foot").setWeighting("fastest"));
			profiles.add(new Profile("balanced_foot").setVehicle("foot").setWeighting("balanced"));
			args.setProfiles(profiles);
			args.putObject("graph.flag_encoders",prop.getProperty("graph.flag_encoders"));
			args.putObject("graph.dataaccess", prop.getProperty("graph.dataaccess"));
		} catch (IOException e) {
			throw new RuntimeException("Config properties are not found. Aborting ...");
		}
    	gh.init(args).setOSMFile(MAP_URL).setGraphHopperLocation("graphLocation");
    	//System.out.println(gh.getEncodingManager().getDecimalEncodedValue("smoke"));
    	gh.clean();
    	gh.importOrLoad();
    	this.boundingBox = gh.getGraphHopperStorage().getBaseGraph().getBounds();
    	dt=new TrafficdatDto(gh,lock.writeLock());
    	rp=new RoutePathDto(gh, lock.readLock());
    	ai=new AirQualityDto(gh,lock.writeLock());
    }
	
    public Trafficdat getAll()
	{
		return dt.getRoads();
	}
	
	public void start()
	{
		if (apiKey==null){
			throw new RuntimeException("API Key for Here Maps is not found. Aborting...");
		}
	    dt.fetchData(apiKey, this.boundingBox);
		ai.readJSON(this.boundingBox);
	}

	public RoutePath getPath(RouteInformation p)
	{
		return rp.find(p);
	}

}
