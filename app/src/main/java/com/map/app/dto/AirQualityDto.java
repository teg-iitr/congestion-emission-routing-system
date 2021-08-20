package com.map.app.dto;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.Snap;
import com.map.app.graphhopperfuncs.airQualityBFS;
import com.map.app.model.airQuality;

public class AirQualityDto {
	private JSONParser jsonP;
	private Lock writeLock;
	private ArrayList<airQuality> ap;
	private final GraphHopper hopper;
	private final String url = "https://api.waqi.info/map/bounds/?latlng=28.5571231169,77.3900417514,28.7009665922,77.1195034214&token=<AQI-API-KEY>";
	private airQualityBFS trav;
	public AirQualityDto(GraphHopper hopper, Lock lock) {
		this.hopper = hopper;
		this.jsonP = new JSONParser();
		this.ap = new ArrayList<>();
		this.writeLock = lock;
	}
	public void readJSON() {
		/*
		 Fetching the content from the api and parsing the json result
		 */
		try {
			URL uri = new URL(url);
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
					ap.add(new airQuality(lat, lon, aqi, name));
				}
			}
			//assign air quality metric to edge in graphhopper
			Graph gh = hopper.getGraphHopperStorage().getBaseGraph();
			LocationIndex locationIndex = hopper.getLocationIndex();
			trav = new airQualityBFS(hopper, gh, ap);
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