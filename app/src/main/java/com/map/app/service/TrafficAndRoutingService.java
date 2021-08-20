package com.map.app.service;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.springframework.stereotype.Service;
import com.graphhopper.GraphHopper;
import com.graphhopper.GraphHopperConfig;
import com.graphhopper.config.Profile;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.ev.UnsignedDecimalEncodedValue;
import com.graphhopper.routing.ev.UnsignedIntEncodedValue;
import com.graphhopper.routing.weighting.custom.CustomProfile;
import com.graphhopper.util.CustomModel;
import com.map.app.dto.AirQualityDto; 
import com.map.app.dto.RouteInformationDto;
import com.map.app.dto.TrafficdatDto;
import com.map.app.dto.routePathDto;
import com.map.app.graphhopperfuncs.MyGraphHopper;
import com.map.app.model.RouteInformation;
import com.map.app.model.routePath;
import com.map.app.model.trafficdat;

@Service
public class TrafficAndRoutingService {
	
	private final ReadWriteLock lock;
	
	private final GraphHopper gh;
	private static final String MAP_URL="maps/NewDelhi.osm.pbf";
	private String apiKey="1c78c959-7a2e-486b-b674-3df4ab665884";
	private AirQualityDto ai;
	private TrafficdatDto dt;
	private routePathDto rp;
	
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
    	FileInputStream ip;
		try {
			ip = new FileInputStream("config.properties");
			prop.load(ip);
			args.putObject("datareader.file",MAP_URL);
			List<Profile> profiles = new ArrayList<>();
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	gh.init(args).setOSMFile(MAP_URL).setGraphHopperLocation("graphLocation");
    	//System.out.println(gh.getEncodingManager().getDecimalEncodedValue("smoke"));
    	gh.clean();
    	gh.importOrLoad();
    	dt=new TrafficdatDto(gh,lock.writeLock());
    	rp=new routePathDto(dt,gh,lock.readLock());
    	ai=new AirQualityDto(gh,lock.writeLock());
    }
	
    public trafficdat getAll()
	{
    	
		return dt.getRoads();
	}
	
	public void start()
	{
	    dt.fetchData();
		ai.readJSON();
			
	}
	public routePath getPath(RouteInformation p)

	{
		routePath path=rp.find(p);
		return path;
	}

}
