package com.map.app.graphhopperfuncs;

import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.AbstractAdjustedWeighting;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.TurnCostProvider;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Calculates the fastest least air polluted route with the specified vehicle (VehicleEncoder). Calculates the weight
 * in seconds.
 * <p>
 *
 * @author Siftee 
 */
public class BalancedWeighting extends FastestWeighting {
    private static final String NAME="balanced";
    private static final String TIME_FACTOR="balanced.time_factor";
    private static final String POLLUTION_FACTOR="balanced.pollution_factor";
    private final double timeFactor;
    private final double pollutionFactor;
    private final DecimalEncodedValue smokeEnc;
    
    public BalancedWeighting(FlagEncoder encoder,PMap map,TurnCostProvider turnCostProvider)
    {

    	super(encoder, map, turnCostProvider);
        Properties prop=new Properties();
        double getTimeFactor, getPollutionFactor;
        try (FileInputStream ip = new FileInputStream("config.properties")) {
            prop.load(ip);
            getTimeFactor = Double.parseDouble(prop.getProperty("balanced_time_factor"));
            getPollutionFactor = Double.parseDouble(prop.getProperty("balanced_pollution_factor"));
        } catch (IOException e) {
            throw new RuntimeException("Config properties are not found. Aborting ...");
        }
        this.timeFactor = checkBounds(TIME_FACTOR, map.getDouble(TIME_FACTOR, getTimeFactor), 0.0D, 100.0D);
        this.pollutionFactor = checkBounds(POLLUTION_FACTOR, map.getDouble(POLLUTION_FACTOR, getPollutionFactor), 0.0D, 1D);
    	smokeEnc=encoder.getDecimalEncodedValue("smoke");
    	if (timeFactor < 1e-5 && pollutionFactor < 1e-5)
            throw new IllegalArgumentException("[" + NAME + "] one of distance_factor or time_factor has to be non-zero");
    }
    static double checkBounds(String key, double val, double from, double to) {
        if (!(val < from) && !(val > to)) {
            return val;
        } else {
            throw new IllegalArgumentException(key + " has invalid range should be within [" + from + ", " + to + "]");
        }
    }
//    public BalancedWeighting(FlagEncoder encoder, double distanceFactor, TurnCostProvider turnCostProvider) {
//        super(encoder, new PMap(), turnCostProvider);
//        this.pollutionFactor = 0.5;
//        this.timeFactor = 20;
//    }

    @Override
    public double calcEdgeWeight(EdgeIteratorState edgeState, boolean reverse) {
        double time = super.calcEdgeWeight(edgeState, reverse);
        double smoke = edgeState.get(smokeEnc);
        double balanced = (time * timeFactor) + (smoke * pollutionFactor * time);

//        if(smoke!=0)
//            System.out.println("balanced smoke " + smoke);
//        System.out.println("balanced time " + time);
        return balanced;
    }
    @Override
    public String getName() {
        return NAME;
    }
}