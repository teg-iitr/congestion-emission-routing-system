package com.map.app.dto;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.graphhopper.util.shapes.BBox;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.graphhopper.GraphHopper;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.index.LocationIndex;
import com.map.app.graphhopperfuncs.AirQualityBFS;
import com.map.app.model.AirQuality;

/**
 * @author Siftee, Amit
 */

public class AirQualityDto {
	private final JSONParser jsonP;
	private final Lock writeLock;
	private final ArrayList<AirQuality> ap;
	private final GraphHopper hopper;
	private final String aqiApiKey = System.getenv("waqi_api_key");
	private static final String url = "https://api.waqi.info/map/bounds/?latlng=";

	public AirQualityDto(GraphHopper hopper, Lock lock) {
		this.hopper = hopper;
		this.jsonP = new JSONParser();
		this.ap = new ArrayList<>();
		this.writeLock = lock;
	}
	public void readJSON(BBox boundingBox) {
		/*
		 Fetching the content from the api and parsing the json result
		 */
		if (aqiApiKey==null){
			throw new RuntimeException("API Key for AQI URL is not found. Aborting...");
		}
		try {
			URL uri = new URL(url+boundingBox.minLat+","+boundingBox.minLon + ","
					+ boundingBox.maxLat + "," + boundingBox.maxLon +"&token=" + aqiApiKey );
			HttpURLConnection con = (HttpURLConnection) uri.openConnection();
			int responseCode = con.getResponseCode();
			if (responseCode != 200) {
				throw new RuntimeException("HttpResponseCode: " + responseCode);
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in .readLine()) != null) {
				response.append(inputLine);
			}
			// System.out.println(response.toString());
			in .close();

			JSONObject obj = (JSONObject) jsonP.parse(response.toString());
			JSONArray data = (JSONArray) obj.get("data");
			//System.out.println(data);
			for (int i = 0; i<data.size(); i++) {
				JSONObject obj1 = (JSONObject) data.get(i);
				double lat = (double) obj1.get("lat");
				double lon = (double) obj1.get("lon");
				String aq = (String) obj1.get("aqi");
				// Regex to check string
				// contains only digits
				String regex = "[0-9]+";

				// Compile the ReGex
				Pattern p = Pattern.compile(regex);

				// If the string is empty
				// return false
				if (aq == null) {
					continue;
				}

				// Find match between given string
				// and regular expression
				// using Pattern.matcher()
				Matcher m = p.matcher(aq);
				if (m.matches()) {
					double aqi = Double.parseDouble(aq);
					JSONObject obj2 = (JSONObject) obj1.get("station");
					String name = (String) obj2.get("name");
					ap.add(new AirQuality(lat, lon, aqi, name));
				}
			}
			//assign air quality metric to edge in graphhopper
			Graph gh = hopper.getGraphHopperStorage().getBaseGraph();
			LocationIndex locationIndex = hopper.getLocationIndex();
			AirQualityBFS trav = new AirQualityBFS(hopper, gh, ap);
			writeLock.lock();
			try {
				trav.start(gh.createEdgeExplorer(), 0);
			} finally {
				writeLock.unlock();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}