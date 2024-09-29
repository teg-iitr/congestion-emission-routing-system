package com.map.app.graphhopperfuncs;

import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.*;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

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
    private final Double timeFactor;
    private final Double pollutionFactor;
    private final DecimalEncodedValue smokeEnc;
    private final DecimalEncodedValue timeEnc;
//    private final

    protected BalancedWeighting(FlagEncoder encoder) {
        this(encoder, NO_TURN_COST_PROVIDER);
    }
    public BalancedWeighting(FlagEncoder encoder, TurnCostProvider turnCostProvider) {
        this(encoder, new PMap(0), turnCostProvider);
    }
    public BalancedWeighting(FlagEncoder encoder, PMap map) {
        this(encoder, map, TurnCostProvider.NO_TURN_COST_PROVIDER);
    }
    public BalancedWeighting(FlagEncoder encoder,PMap map,TurnCostProvider turnCostProvider)
    {
        super(encoder, turnCostProvider);
        Properties prop=new Properties();
        Double getTimeFactor, getPollutionFactor;
        try (FileInputStream ip = new FileInputStream("config.properties")) {
            prop.load(ip);
            getTimeFactor = Double.parseDouble(prop.getProperty("balanced_time_factor"));
            getPollutionFactor = Double.parseDouble(prop.getProperty("balanced_pollution_factor"));
        } catch (IOException e) {
            throw new RuntimeException("Config properties are not found. Aborting ...");
        }
        getTimeFactor = getTimeFactor / (getTimeFactor + getPollutionFactor);
        getPollutionFactor = 1 - getTimeFactor;
        this.timeFactor = checkBounds(TIME_FACTOR, map.getDouble(TIME_FACTOR, getTimeFactor), 0.0D, 1D);
        this.pollutionFactor = checkBounds(POLLUTION_FACTOR, map.getDouble(POLLUTION_FACTOR, getPollutionFactor), 0.0D, 1D);
        smokeEnc=encoder.getDecimalEncodedValue("smoke");
        timeEnc=encoder.getDecimalEncodedValue("time");
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

    @Override
    public double calcEdgeWeight(EdgeIteratorState edgeState, boolean reverse) {
        double smoke = edgeState.get(smokeEnc);
        double timeG = edgeState.get(timeEnc);
        double timeT = super.calcEdgeWeight(edgeState, reverse);
        // the magnitude of (smoke * pollutionFactor * timeG) is very high compared to (timeFactor * timeT)
        int power10 = countDigit((long) (smoke * pollutionFactor * timeG));
        // makes (timeFactor * timeT) in the same range by multiplying with 10^(number of digits)
        double fastestWeight = timeFactor * timeT * Math.pow(10, power10);
        double greenestWeight = smoke * pollutionFactor * timeG;
        return fastestWeight + greenestWeight;
    }

    @Override
    public String getName() {
        return NAME;
    }

    // to make the magnitude (range) of a number similar to another number.
    static int countDigit(long n)
    {
        int count = 0;
        while (n != 0) {
            n = n / 10;
            ++count;
        }
        return count - 1;
    }
}
