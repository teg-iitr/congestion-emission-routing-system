package com.map.app.graphhopperfuncs;

import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.TurnCostProvider;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

import static com.graphhopper.routing.weighting.TurnCostProvider.NO_TURN_COST_PROVIDER;

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
    private DecimalEncodedValue smokeEnc;
    
    public BalancedWeighting(FlagEncoder encoder,PMap map,TurnCostProvider turnCostProvider)
    {
    	super(encoder, map, turnCostProvider);
    	timeFactor=checkBounds(TIME_FACTOR, map.getDouble(TIME_FACTOR, 1), 0, 10);
    	pollutionFactor=checkBounds(POLLUTION_FACTOR, map.getDouble(POLLUTION_FACTOR, 0.07), 0, 10);
    	smokeEnc=encoder.getDecimalEncodedValue("smoke");
    	if (timeFactor < 1e-5 && pollutionFactor < 1e-5)
            throw new IllegalArgumentException("[" + NAME + "] one of distance_factor or time_factor has to be non-zero");
    }
    public BalancedWeighting(FlagEncoder encoder, double pollutionFactor) {
        this(encoder, pollutionFactor, NO_TURN_COST_PROVIDER);
    }

    public BalancedWeighting(FlagEncoder encoder, double pollutionFactor, TurnCostProvider turnCostProvider) {
        super(encoder, new PMap(), turnCostProvider);
        this.pollutionFactor = checkBounds(POLLUTION_FACTOR, pollutionFactor, 0, 10);
        this.timeFactor = 1;
    }
    static double checkBounds(String key, double val, double from, double to) {
        if (val < from || val > to)
            throw new IllegalArgumentException(key + " has invalid range should be within [" + from + ", " + to + "]");
        return val;
    }
    @Override
    public double calcEdgeWeight(EdgeIteratorState edgeState, boolean reverse) {
        double time = super.calcEdgeWeight(edgeState, reverse);
        return time * timeFactor + edgeState.get(smokeEnc) * pollutionFactor;
    }
    @Override
    public String getName() {
        return NAME;
    }
}