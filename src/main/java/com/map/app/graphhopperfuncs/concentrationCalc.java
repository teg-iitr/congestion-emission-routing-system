package com.map.app.graphhopperfuncs;

import java.util.List;

import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.details.PathDetail;
import com.map.app.service.TransportMode;

public class concentrationCalc {
	/*public static double convToAQI(double conc)
	{
		if(conc<30)
		{
			return conc*1.67;
		}
		else if(conc>=30 && conc<60)
		{
			return 50+(1.67*(conc-30));
		}
		else if(conc>=60 && conc<90)
		{
			return 100+(3.33*(conc-60));
		}
		else if(conc>=90 && conc<120)
		{
			return 200+(3.33*(conc-90));
		}
		else if(conc>-120 && conc<=250)
		{
			return 300+(0.77*(conc-120));
		}
		else
		{
			return 400+(0.77*(conc-250));
		}
	}*/
	public static double calcConcentrationScore(GraphHopper gh,List<PathDetail> pathDetails,TransportMode mode)
	{
		int score=0;
		Graph g=gh.getGraphHopperStorage().getBaseGraph();
		for (PathDetail detail : pathDetails) {
			FlagEncoder encoder = gh.getEncodingManager().getEncoder(mode.toString());
			DecimalEncodedValue smokeEnc = encoder.getDecimalEncodedValue("smoke");
			EdgeIteratorState edge = g.getEdgeIteratorState((Integer)detail.getValue(), Integer.MIN_VALUE);
			//convToConc() System.out.println(edge.get(smokeEnc));
			score+=smokeEnc.getDecimal(false, edge.getFlags());
			}
		
		//System.out.println(pathDetails.size());
//		System.out.println(score);
		//return score;
		return score;
	}
}
