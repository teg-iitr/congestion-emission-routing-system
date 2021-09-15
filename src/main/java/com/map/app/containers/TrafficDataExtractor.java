package com.map.app.containers;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.graphhopper.util.shapes.BBox;
import com.map.app.service.TransportMode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeIteratorState;
import com.map.app.model.TrafficData;

/**
 * @author  Siftee, Amit
 */
public class TrafficDataExtractor {
	private TrafficData dt = new TrafficData();

	private final Lock writeLock;
	private final GraphHopper hopper;
	public TrafficDataExtractor(GraphHopper hopper, Lock lock) {
		this.hopper = hopper;
		this.writeLock = lock;
	}

	public void fetchData(String apiKey, BBox boundingBox) {
		final String URL = "https://traffic.ls.hereapi.com/traffic/6.2/flow.xml?apiKey="
				+apiKey +"&bbox=" +
				+boundingBox.minLat+","+boundingBox.minLon + ";"
				+ boundingBox.maxLat + "," + boundingBox.maxLon +
				"&responseattributes=sh,fc&units=metric";
		parse_XML(URL);
	}

	public void feed(TrafficData tempdt) {
		writeLock.lock();
		try {
			lockedFeed(tempdt);
		} finally {
			writeLock.unlock();
		}
	}

	private void lockedFeed(TrafficData tempdt) {
		this.dt = tempdt;
		Graph graph = hopper.getGraphHopperStorage().getBaseGraph();
		for (TransportMode mode: TransportMode.values()) {
			FlagEncoder encoder = hopper.getEncodingManager().getEncoder(mode.toString());
			LocationIndex locationIndex = hopper.getLocationIndex();
			int errors = 0;
			int updates = 0;
			Set<Integer> edgeIds = new HashSet<> ();
			for (int i = 0; i<dt.getLat().size(); i++) {
				List<Float> entryLats = dt.getLat().get(i);
				List<Float> entryLons = dt.getLons().get(i);
				List<Float> entrySpeed = dt.getSpeed().get(i);
				Float latitude = entryLats.get(entryLats.size() / 2);
				Float longitude = entryLons.get(entryLons.size() / 2);
				Snap qr = locationIndex.findClosest(latitude, longitude, EdgeFilter.ALL_EDGES);
				if (!qr.isValid()) {
					// logger.info("no matching road found for entry " + entry.getId() + " at " + point);
					errors++;
					continue;
				}
				int edgeId = qr.getClosestEdge().getEdge();
				if (edgeIds.contains(edgeId)) {
					// TODO this wouldn't happen with our map matching component
					errors++;
					continue;
				}
				edgeIds.add(edgeId);
				EdgeIteratorState edge = graph.getEdgeIteratorState(edgeId, Integer.MIN_VALUE);
				double value = entrySpeed.get(0);
				DecimalEncodedValue avgSpeedEnc = encoder.getAverageSpeedEnc();
				double oldValue = edge.get(avgSpeedEnc);
				if (value != oldValue) {
					updates++;
					edge.set(avgSpeedEnc, Math.min(value, avgSpeedEnc.getMaxDecimal()));
				}
			}
		}
	}

	private void parse_XML(String Url) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new URL(Url).openStream());
			NodeList roads = doc.getElementsByTagName("FI");
			TrafficData tempdt = new TrafficData();
			tempdt.setSpeed(new ArrayList<>());
			tempdt.setLat(new ArrayList<>());
			tempdt.setLons(new ArrayList<>());

			for (int i = 0; i<roads.getLength(); i++) {
				Node road_1 = roads.item(i);
				if (road_1.getNodeType() == Node.ELEMENT_NODE) {
					Element road = (Element) road_1;
					float le = 0;
					float fc = 5;
					float su = 0;
					float cn = 0;
					float ff = 0;
					NodeList myxml = road.getChildNodes();
					for (int j = 0; j<myxml.getLength(); j++) {
						Node child_1 = myxml.item(j);
						if (child_1.getNodeType() == Node.ELEMENT_NODE) {
							Element child = (Element) child_1;
							if (child.hasAttribute("LE")) {
								le = Float.parseFloat(child.getAttribute("LE"));
							}
							if (child.hasAttribute("FC")) {
								fc = Float.parseFloat(child.getAttribute("FC"));
							}
							if (child.hasAttribute("CN")) {
								cn = Float.parseFloat(child.getAttribute("CN"));
							}
							if (child.hasAttribute("SU")) {
								su = Float.parseFloat(child.getAttribute("SU"));
							}
							if (child.hasAttribute("FF")) {
								ff = Float.parseFloat(child.getAttribute("FF"));
							}

						}
					}
					if (cn >= 0.7 && fc<= 4) {

						NodeList shps = road.getElementsByTagName("SHP");
						if (shps.getLength() > 0) {
							ArrayList<Float> las = new ArrayList<>();
							ArrayList<Float> longs = new ArrayList<>();
							ArrayList<Float> combospeed = new ArrayList<>();

							for (int k = 0; k<shps.getLength(); k++) {
								Node shp_1 = shps.item(k);
								if (shp_1.getNodeType() == Node.ELEMENT_NODE) {
									Element shp = (Element) shp_1;
									String[] ans = shp.getTextContent().replace(',', ' ').split(" ");

									if (k == 0) {
										las.add(Float.parseFloat(ans[0]));
										longs.add(Float.parseFloat(ans[1]));
									}

									for (int l = 1; l<(int) ans.length / 2; l++) {
										las.add(Float.parseFloat(ans[2 * l]));
										longs.add(Float.parseFloat(ans[2 * l + 1]));
									}
								}
							}
							tempdt.getLat().add(las);
							tempdt.getLons().add(longs);
							combospeed.add(su);
							combospeed.add(ff);
							tempdt.getSpeed().add(combospeed);

						}
					}

				}

			}
			feed(tempdt);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public TrafficData getRoads() {
		return dt;
	}

}