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
	
	private final ReadWriteLock lock;
	
	private final GraphHopper gh;
	//private static final String MAP_URL="maps/NewDelhi.osm.pbf";
	private final String apiKey;
	
	private final AirQualityDataExtractor ai;
	private final TrafficDataExtractor dt;
	private final RoutePathContainer rp;
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
			System.out.println("Using OSM file "+ prop.getProperty("datareader.file"));
			args.putObject("datareader.file",prop.getProperty("datareader.file"));
			List<Profile> profiles = new ArrayList<>();

			for (PathChoice pc : PathChoice.values()) {
				for (TransportMode tm : TransportMode.values()) {
					profiles.add(new Profile(TrafficAndRoutingService.getModeBasedPathChoice(pc, tm)).setVehicle(tm.toString()).setWeighting(pc.toString()));
				}
			}
			
			args.setProfiles(profiles);
			args.putObject("graph.flag_encoders",prop.getProperty("graph.flag_encoders"));
			args.putObject("graph.dataaccess", prop.getProperty("graph.dataaccess"));
			apiKey=prop.getProperty("here_api_key");
		} catch (IOException e) {
			throw new RuntimeException("Config properties are not found. Aborting ...");
		}
    	gh.init(args).setGraphHopperLocation("graphLocation");
    	//System.out.println(gh.getEncodingManager().getDecimalEncodedValue("smoke"));
    	gh.clean();
    	gh.importOrLoad();
    	this.boundingBox = gh.getGraphHopperStorage().getBaseGraph().getBounds();
    	dt=new TrafficDataExtractor(gh,lock.writeLock());
    	rp=new RoutePathContainer(gh, lock.readLock());
    	ai=new AirQualityDataExtractor(gh,lock.writeLock());
    }
	
	public static String getModeBasedPathChoice(PathChoice pathChoice, TransportMode transportMode) {
		return pathChoice.toString().concat("_").concat(transportMode.toString());
	}
	
	public ArrayList<Float> getBoundingBox() {
		ArrayList<Float> box=new ArrayList<>();
		box.add((float)boundingBox.minLat);
		box.add((float)boundingBox.minLon);
		box.add((float) boundingBox.maxLat);
		box.add((float)boundingBox.maxLon);
		return box;
	}
    public TrafficData getAll()
	{
		return dt.getRoads();
	}
	
	public void start()
	{
		if (apiKey.equals("<HERE_API_KEY>")){
			throw new RuntimeException("API Key for Here Maps is not found. Aborting...");
		}
	    dt.fetchData(apiKey, this.boundingBox);
		ai.readJSON(this.boundingBox);
	}

	public RoutePath getPath(UrlContainer p)
	{
		return rp.find(p);
	}

}
