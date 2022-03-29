package com.map.app.graphhopperfuncs;

import java.util.List;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.details.PathDetail;
import com.map.app.service.TransportMode;

public class EncoderCalculator {

	public static double calcConcentrationScore(GraphHopper gh,List<PathDetail> pathDetails,TransportMode mode)
	{
		int score = 0;
		Graph g=gh.getGraphHopperStorage().getBaseGraph();
		for (PathDetail detail : pathDetails) {
			FlagEncoder encoder = gh.getEncodingManager().getEncoder(mode.toString());
			DecimalEncodedValue smokeEnc = encoder.getDecimalEncodedValue("smoke");
			EdgeIteratorState edge = g.getEdgeIteratorState((Integer)detail.getValue(), Integer.MIN_VALUE);
			score+=edge.get(smokeEnc);
			}
		return score;
	}
	public static double calcExposureScore(GraphHopper gh,List<PathDetail> pathDetails,TransportMode mode)
	{
		double score = 0;
		Graph g=gh.getGraphHopperStorage().getBaseGraph();
		for (PathDetail detail : pathDetails) {
			FlagEncoder encoder = gh.getEncodingManager().getEncoder(mode.toString());
			DecimalEncodedValue smokeEnc = encoder.getDecimalEncodedValue("smoke");
			DecimalEncodedValue timeEnc = encoder.getDecimalEncodedValue("time");
			EdgeIteratorState edge = g.getEdgeIteratorState((Integer)detail.getValue(), Integer.MIN_VALUE);
			score = score + edge.get(smokeEnc) * edge.get(timeEnc);
		}
		return score / Math.pow(10, 3);
	}
	public static double calcTimeScore(GraphHopper gh,List<PathDetail> pathDetails,TransportMode mode)
	{
		double score = 0;
		Graph g=gh.getGraphHopperStorage().getBaseGraph();
		for (PathDetail detail : pathDetails) {
			FlagEncoder encoder = gh.getEncodingManager().getEncoder(mode.toString());
			DecimalEncodedValue timeEnc = encoder.getDecimalEncodedValue("time");
			EdgeIteratorState edge = g.getEdgeIteratorState((Integer)detail.getValue(), Integer.MIN_VALUE);
			score = score + edge.get(timeEnc);
		}
		return score;
	}
}
