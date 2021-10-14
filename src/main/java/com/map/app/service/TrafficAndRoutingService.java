package com.map.app.service;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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
	private String apiKey = System.getenv("here_api_key");
	
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

			profiles.add(new Profile("ipt").setVehicle("car").setWeighting("fastest"));

			// see https://github.com/graphhopper/graphhopper/blob/4.x/docs/core/custom-models.md
			CustomModel bus_custom_model = new CustomModel();
			bus_custom_model.addToSpeed(Statement.If( "road_class == RESIDENTIAL", Statement.Op.LIMIT, 0.1));
			profiles.add(new CustomProfile("bus").setCustomModel(bus_custom_model).setVehicle("car"));

			CustomModel metro_custom_model = new CustomModel();
			metro_custom_model.addToSpeed(Statement.If( "road_class != TRUNK", Statement.Op.LIMIT, 0.1));
			profiles.add(new CustomProfile("metro").setCustomModel(metro_custom_model).setVehicle("car"));

			args.setProfiles(profiles);
			args.putObject("graph.flag_encoders",prop.getProperty("graph.flag_encoders"));
			args.putObject("graph.dataaccess", prop.getProperty("graph.dataaccess"));
			if( apiKey==null) apiKey =prop.getProperty("here_api_key"); // the api key must be in either system env or config.properties
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
