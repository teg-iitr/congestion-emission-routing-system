package com.map.app.graphhopperfuncs;
import java.util.ArrayList;

import com.graphhopper.GraphHopper;
import com.graphhopper.coll.GHBitSet;
import com.graphhopper.coll.GHBitSetImpl;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.SimpleIntDeque;
import com.graphhopper.util.XFirstSearch;
import com.map.app.model.AirQuality;
import com.map.app.service.TransportMode;

/**
 * @author Siftee, Author
 */
public class AirQualityBFS extends XFirstSearch {
	//does a BFS traversal and assigns edge with air quality value as average of aqi value of base and adjacent node.
	private final Graph gh;
	private final GraphHopper hopper;
	private final ArrayList<AirQuality> ap;
	public AirQualityBFS(GraphHopper hopper, Graph gh, ArrayList<AirQuality> ap) {
		this.gh = gh;
		this.hopper = hopper;
		this.ap = ap;
	}
	@Override
	protected GHBitSet createBitSet() {
		return new GHBitSetImpl();
	}

	@Override
	public void start(EdgeExplorer explorer, int temp) {
		for (TransportMode encoder: TransportMode.values()) {
			FlagEncoder Encoder = hopper.getEncodingManager().getEncoder(encoder.toString());
			DecimalEncodedValue smokeEnc = Encoder.getDecimalEncodedValue("smoke");
			SimpleIntDeque fifo = new SimpleIntDeque();
			GHBitSet visited = createBitSet();
			for (int startNode = 0; startNode<gh.getNodes(); startNode++) {
				if (!visited.contains(startNode)) {
					visited.add(startNode);
					fifo.push(startNode);
					BFS(explorer, visited, fifo, smokeEnc);
				}
			}
		}
	}
	private void BFS(EdgeExplorer explorer, GHBitSet visited, SimpleIntDeque fifo, DecimalEncodedValue smokeEnc) {
		int current;
		while (!fifo.isEmpty()) {
			current = fifo.pop();
			if (!goFurther(current))
				continue;
			EdgeIterator iter = explorer.setBaseNode(current);
			while (iter.next()) {
				int connectedId = iter.getAdjNode();
				if (!visited.contains(connectedId)) {
					double base_lat = gh.getNodeAccess().getLat(current);
					double base_lon = gh.getNodeAccess().getLon(current);
					double airQualityBase = IDW(base_lat, base_lon);
					double adj_lat = gh.getNodeAccess().getLat(connectedId);
					double adj_lon = gh.getNodeAccess().getLon(connectedId);
					double airQualityAdj = IDW(adj_lat, adj_lon);
					EdgeIteratorState edge = gh.getEdgeIteratorState(iter.getEdge(), Integer.MIN_VALUE);

					if (Double.isNaN(airQualityAdj) || Double.isNaN(airQualityBase)) {
						edge.set(smokeEnc, 0.);
					} else {
						edge.set(smokeEnc, (airQualityBase + airQualityAdj) / 2);
					}
				}
				if (checkAdjacent(iter) && !visited.contains(connectedId)) {
					visited.add(connectedId);
					fifo.push(connectedId);
				}
			}
		}
	}

	private double IDW(double fromlat, double fromlon) {
		double numer = 0;
		double denom = 0;
		double exp = 1;
		for (AirQuality point: ap) {
			double v = point.getAqi();
			double d = haversine(point.getLat(), point.getLon(), fromlat, fromlon);
			numer = numer + v / Math.pow(d, exp);
			denom = denom + 1 / Math.pow(d, exp);
		}
		return numer / denom;
	}
	private double haversine(double lat1, double lon1, double lat2, double lon2) {
		// distance between latitudes and longitudes
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);

		// convert to radians
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		// apply formulae
		double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
		double rad = 6371;
		double c = 2 * Math.asin(Math.sqrt(a));
		return rad * c;
	}

}