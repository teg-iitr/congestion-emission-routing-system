package com.map.app.graphhopperfuncs;

import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.AbstractWeighting;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.TurnCostProvider;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIteratorState;
import com.map.app.containers.AirQualityDataExtractor;
import com.map.app.model.AirQuality;

import static com.graphhopper.routing.weighting.TurnCostProvider.NO_TURN_COST_PROVIDER;

import java.util.ArrayList;


/**
 * @author Siftee
 */
public class GreenestWeighting extends FastestWeighting {
	private static final String NAME="greenest";
	private final DecimalEncodedValue smokeEnc;
	//final DecimalEncodedValue avgSpeedEnc;
    //private static int avgCount=0;
	
	
	protected GreenestWeighting(FlagEncoder encoder) {
		this(encoder,NO_TURN_COST_PROVIDER);
	}
	public GreenestWeighting(FlagEncoder flagEncoder, TurnCostProvider TurnCostProvider) {
		super(flagEncoder, TurnCostProvider);
		smokeEnc=flagEncoder.getDecimalEncodedValue("smoke");
	//    avgSpeedEnc=flagEncoder.getAverageSpeedEnc();
    }
	@Override
	public double getMinWeight(double distance) {
		return 0;
	}
	@Override
	public String getName() {
		return NAME;
	}
	
	
	@Override
	public double calcEdgeWeight(EdgeIteratorState edgeState, boolean reverse) {
		//System.out.println(edgeState.)
	//	if(edgeState.get(smokeEnc)==0)
		//{
			//count++;
		//	System.out.println(count);
		//}
		//return edgeState.get(smokeEnc);
		//double val=AirQualityDataExtractor.assignWeight(edgeState);
		//System.out.println(val);
		//edgeState.set(smokeEnc,val);
		//System.out.println(edgeState.get(smokeEnc));
		//return val;
		
		//return edgeState.get(smokeEnc);
		double smoke = smokeEnc.getDecimal(reverse, edgeState.getFlags());
		if(smoke!=0)
			 System.out.println("greenest smoke " + smoke);
		System.out.println("greenest time " + super.calcEdgeWeight(edgeState, reverse));
        return smoke * super.calcEdgeWeight(edgeState, reverse);
	}
	
}