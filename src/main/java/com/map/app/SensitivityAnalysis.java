package com.map.app;

import com.graphhopper.GraphHopper;
import com.graphhopper.GraphHopperConfig;
import com.graphhopper.config.Profile;
import com.graphhopper.util.shapes.BBox;
import com.map.app.containers.AirQualityDataExtractor;
import com.map.app.containers.RoutePathContainer;
import com.map.app.containers.TrafficDataExtractor;
import com.map.app.containers.UrlTransformer;
import com.map.app.graphhopperfuncs.MyGraphHopper;
import com.map.app.model.RoutePath;
import com.map.app.model.UrlContainer;
import com.map.app.service.TrafficAndRoutingService;
import com.map.app.service.TransportMode;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.map.app.service.TrafficAndRoutingService.SpeedChoice.*;

public class SensitivityAnalysis {
    public static void main(String[] args) {

        TrafficAndRoutingService trafficAndRoutingService = new TrafficAndRoutingService();
        double pollutionFactor, timeFactor;
        for (timeFactor = 0.0; timeFactor <= 1.0; timeFactor += 0.10) {
            for (pollutionFactor = 0.0; pollutionFactor <= 1.0; pollutionFactor += 0.10) {
//                System.out.println((double) Math.round(timeFactor * 100) / 100);
//                System.out.println((double) Math.round(pollutionFactor * 100) / 100);
                trafficAndRoutingService.start();
                UrlTransformer urlTransformer = new UrlTransformer();
                urlTransformer.setStartLoc("77.2060561,28.7152338");
                urlTransformer.setEndLoc("77.1953703,28.5741575");
                urlTransformer.setRouteType("balanced");
                urlTransformer.setVehicle("car");
                UrlContainer urlContainer = urlTransformer.convert();
                RoutePathContainer routePathContainer = trafficAndRoutingService.getRoutePathContainer();
                routePathContainer.setGetTimeFactor((double) Math.round(timeFactor * 100) / 100);
                routePathContainer.setGetPollutionFactor((double) Math.round(pollutionFactor * 100) / 100);
                routePathContainer.finalPath(urlContainer, "balanced_car", TransportMode.car);
                System.out.println("end of iteration");
            }
        }
    }
}
