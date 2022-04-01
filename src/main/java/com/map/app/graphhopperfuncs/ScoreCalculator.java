package com.map.app.graphhopperfuncs;

import java.util.List;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.details.PathDetail;
import com.map.app.service.TransportMode;

public class ScoreCalculator extends FastestWeighting{


	public ScoreCalculator(FlagEncoder encoder) {
		super(encoder);
	}

	public static double calcConcentrationScore(GraphHopper gh, List<PathDetail> pathDetails, TransportMode mode)
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
	public static double calcGreenestTimeScore(GraphHopper gh, List<PathDetail> pathDetails, TransportMode mode)
	{
		double score = 0;
		Graph g=gh.getGraphHopperStorage().getBaseGraph();
		for (PathDetail detail : pathDetails) {
			FlagEncoder encoder = gh.getEncodingManager().getEncoder(mode.toString());
			DecimalEncodedValue timeEnc = encoder.getDecimalEncodedValue("time");
			EdgeIteratorState edge = g.getEdgeIteratorState((Integer)detail.getValue(), Integer.MIN_VALUE);
			score = score + edge.get(timeEnc);
		}
		return (double) Math.round(score * 100 / 60) / 100;
	}
	/*public double calcFastestShortestTimeScore(GraphHopper gh, List<PathDetail> pathDetails, TransportMode mode)
	{
		double score = 0;
		Graph g=gh.getGraphHopperStorage().getBaseGraph();
		for (PathDetail detail : pathDetails) {
			EdgeIteratorState edge = g.getEdgeIteratorState((Integer)detail.getValue(), Integer.MIN_VALUE);
			score = score + super.calcEdgeWeight(edge, false);
		}
		return (double) Math.round(score * 100 / 60) / 100;
	}
	public double calcBalancedTimeScore(GraphHopper gh, List<PathDetail> pathDetails, TransportMode mode, double timeFactor, double pollutionFactor)
	{
		double score = 0;
		Graph g=gh.getGraphHopperStorage().getBaseGraph();
		for (PathDetail detail : pathDetails) {
			FlagEncoder encoder = gh.getEncodingManager().getEncoder(mode.toString());
			DecimalEncodedValue timeEnc = encoder.getDecimalEncodedValue("time");
			EdgeIteratorState edge = g.getEdgeIteratorState((Integer)detail.getValue(), Integer.MIN_VALUE);
			double timeG = edge.get(timeEnc) ;
			double timeF = super.calcEdgeWeight(edge, false);
			score = score + (timeG * pollutionFactor) + (timeF * timeFactor);
		}
		return (double) Math.round(score * 100 / 60) / 100;
	}*/
}
