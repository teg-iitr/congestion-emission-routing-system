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
	public static double calcConcentrationScore(GraphHopper gh,List<PathDetail> pathDetails,List<PathDetail> pathDetailsTime,TransportMode mode)
	{
		double score=0;
		double sum=0;
		//System.out.println(pathDetailsConc);
		//System.out.println(pathDetailsTime);
		
		//System.out.println(pathDetailsConc.size()+" "+pathDetailsTime.size());
		
		Graph g=gh.getGraphHopperStorage().getBaseGraph();
		for (int i=0;i<pathDetails.size();i++) {
			//System.out
			PathDetail detail=pathDetails.get(i);
			PathDetail time=pathDetailsTime.get(i);
			FlagEncoder encoder = gh.getEncodingManager().getEncoder(mode.toString());
			DecimalEncodedValue smokeEnc = encoder.getDecimalEncodedValue("smoke");
			EdgeIteratorState edge = g.getEdgeIteratorState((Integer)detail.getValue(), Integer.MIN_VALUE);
			//convToConc() 
			//System.out.println(edge.getDistance()+" "+edge.get(smokeEnc));
			//System.out.println((Long)time.getValue());
			score+=smokeEnc.getDecimal(false, edge.getFlags())*(((Long)time.getValue())/36000);
			//System.out.println(smokeEnc.getDecimal(false, edge.getFlags()));
			//sum+=(Long)time.getValue();
			}
		
		//System.out.println(pathDetails.size());
		//System.out.println(score);
		//System.out.println(sum);
		
		//return score;
		return score;//sum;
	}
}
