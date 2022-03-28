package com.map.app.graphhopperfuncs;

import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.AbstractWeighting;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.TurnCostProvider;
import com.graphhopper.routing.weighting.custom.CustomWeightingHelper;
import com.graphhopper.util.EdgeIteratorState;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static com.graphhopper.routing.weighting.TurnCostProvider.NO_TURN_COST_PROVIDER;


/**
 * @author Siftee
 */
public class GreenestWeighting extends AbstractWeighting {
	private static final String NAME="greenest";
	private final DecimalEncodedValue smokeEnc;
	private final DecimalEncodedValue timeEnc;
	//final DecimalEncodedValue avgSpeedEnc;
	//private static int avgCount=0;


	protected GreenestWeighting(FlagEncoder encoder) {
		this(encoder,NO_TURN_COST_PROVIDER);
	}

	public GreenestWeighting(FlagEncoder flagEncoder, TurnCostProvider turnCostProvider) {
		super(flagEncoder, turnCostProvider);
		smokeEnc=flagEncoder.getDecimalEncodedValue("smoke");
		timeEnc=flagEncoder.getDecimalEncodedValue("time");
		//    avgSpeedEnc=flagEncoder.getAverageSpeedEnc();
	}
	@Override
	public double getMinWeight(double distance) {
		Properties prop=new Properties();
		int defaultSmoke;
		try (FileInputStream ip = new FileInputStream("config.properties")) {
			prop.load(ip);
			defaultSmoke = Integer.parseInt(prop.getProperty("default_smoke"));
		} catch (IOException e) {
			throw new RuntimeException("Config properties are not found. Aborting ...");
		}
		return defaultSmoke;
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
		double smoke = edgeState.get(smokeEnc);
		double time = edgeState.get(timeEnc);

//		System.out.println(time);
//		if(smoke!=defaultSmoke)
//			System.out.println("greenest smoke " + smoke);
//		System.out.println("greenest time " + super.calcEdgeWeight(edgeState, reverse));
		return time * smoke;
	}

}
