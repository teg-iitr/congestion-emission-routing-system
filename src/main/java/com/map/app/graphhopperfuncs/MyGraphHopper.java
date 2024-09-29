package com.map.app.graphhopperfuncs;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.WeightingFactory;

/**
 * @author Siftee
 */
public class MyGraphHopper extends GraphHopper {
	//necessary to make a new instance of graphhopper to assign custom weighting options
	
	@Override
	protected WeightingFactory createWeightingFactory() {
		return new MyWeightingFactory(this.getGraphHopperStorage(), this.getEncodingManager());
	}
}