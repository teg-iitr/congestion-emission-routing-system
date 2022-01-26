package com.map.app.graphhopperfuncs;

import java.util.List;

import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.details.PathDetail;
import com.map.app.service.TransportMode;

public class concentrationCalc {
	public static double convToConc(double aqi)
	{
		if(aqi>=0 && aqi<=50)		
		{			return aqi*0.308;		
		}		
		else if(aqi>=51 && aqi<=100)		
		{			
			return ((aqi-51)*0.508) + 15.5;		
		}		
		else if(aqi>=101 && aqi<=150)		
		{			
			return ((aqi-101)*0.508)+40.5;		
		}		
		else if(aqi>=151 && aqi<=200)	
		{			
			return ((aqi-151)*1.73)+65.5;		
		}		
		else if(aqi>=201 && aqi<=300)		
		{			
			return ((aqi-201)*1.009)+150.5;		
		}		
		else if(aqi>=301 && aqi<=400) 
			return ((aqi-301)*1.009)+250.2; 
		else		
		{	
			return ((aqi-401)*1.51)+350.5;		
		}
	}
	public static double calcConcentrationScore(GraphHopper gh,List<PathDetail> pathDetails,TransportMode mode)
	{
		int score=0;
		Graph g=gh.getGraphHopperStorage().getBaseGraph();
		for (PathDetail detail : pathDetails) {
			FlagEncoder encoder = gh.getEncodingManager().getEncoder(mode.toString());
			DecimalEncodedValue smokeEnc = encoder.getDecimalEncodedValue("smoke");
			EdgeIteratorState edge = g.getEdgeIteratorState((Integer)detail.getValue(), Integer.MIN_VALUE);
			//convToConc() System.out.println(edge.get(smokeEnc));
			score+=convToConc(edge.get(smokeEnc));
			}
		return score;
	}
}
