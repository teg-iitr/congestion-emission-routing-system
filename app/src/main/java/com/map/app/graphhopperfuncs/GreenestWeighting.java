package com.map.app.graphhopperfuncs;

import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.AbstractWeighting;
import com.graphhopper.routing.weighting.TurnCostProvider;
import com.graphhopper.util.EdgeIteratorState;
import static com.graphhopper.routing.weighting.TurnCostProvider.NO_TURN_COST_PROVIDER;

import org.locationtech.jts.planargraph.Edge;


public class GreenestWeighting extends AbstractWeighting {
	private static final String NAME="greenest";
	private DecimalEncodedValue smokeEnc;
	DecimalEncodedValue avgSpeedEnc;
    
	protected GreenestWeighting(FlagEncoder encoder) {
		this(encoder,NO_TURN_COST_PROVIDER);
	}
	public GreenestWeighting(FlagEncoder flagEncoder, TurnCostProvider TurnCostProvider) {
		super(flagEncoder, TurnCostProvider);
		smokeEnc=flagEncoder.getDecimalEncodedValue("smoke");
	    avgSpeedEnc=flagEncoder.getAverageSpeedEnc();
    }

	
	@Override
	public double getMinWeight(double distance) {
		return distance;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public double calcEdgeWeight(EdgeIteratorState edgeState, boolean reverse) {
		double smokeValue=edgeState.get(smokeEnc);
		return smokeValue;
	}

}
