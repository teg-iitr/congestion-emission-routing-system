package com.map.app.graphhopperfuncs;

import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.TurnCostProvider;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

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
    	timeFactor=checkBounds(TIME_FACTOR, map.getDouble(TIME_FACTOR, 0.1));
    	pollutionFactor=checkBounds(POLLUTION_FACTOR, map.getDouble(POLLUTION_FACTOR, 0.07));
    	smokeEnc=encoder.getDecimalEncodedValue("smoke");
    	if (timeFactor < 1e-5 && pollutionFactor < 1e-5)
            throw new IllegalArgumentException("[" + NAME + "] one of distance_factor or time_factor has to be non-zero");
    }

    static double checkBounds(String key, double val) {
        if (val < (double) 0 || val > (double) 10)
            throw new IllegalArgumentException(key + " has invalid range should be within [" + (double) 0 + ", " + (double) 10 + "]");
        return val;
    }
    @Override
    public double calcEdgeWeight(EdgeIteratorState edgeState, boolean reverse) {
        double time = super.calcEdgeWeight(edgeState, reverse);
        double smoke = smokeEnc.getDecimal(reverse, edgeState.getFlags());
//        if(smoke!=0)
//            System.out.println("balanced smoke " + smoke);
//        System.out.println("balanced time " + time);
        return (time * timeFactor) + (smoke * pollutionFactor * time);
    }
    @Override
    public String getName() {
        return NAME;
    }
}