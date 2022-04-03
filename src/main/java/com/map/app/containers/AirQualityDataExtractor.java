package com.map.app.containers;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.graphhopper.util.shapes.BBox;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.graphhopper.GraphHopper;
import com.graphhopper.storage.Graph;
import com.map.app.graphhopperfuncs.AirQualityBFS;
import com.map.app.model.AirQuality;

/**
 * @author Siftee, Amit
 */

public class AirQualityDataExtractor {
	private final JSONParser jsonP;
	private final Lock writeLock;
	private final GraphHopper hopper;
	private String aqiApiKey = System.getenv("waqi_api_key");
	private static final String url = "https://api.waqi.info/map/bounds/?latlng=";

	public AirQualityDataExtractor(GraphHopper ghopper, Lock lock) {
		hopper = ghopper;
		this.jsonP = new JSONParser();
		this.writeLock = lock;
		if (aqiApiKey ==null) {

			Properties prop=new Properties();

			try(FileInputStream fileInputStream = new FileInputStream("config.properties")) {
				prop.load(fileInputStream);

				aqiApiKey = prop.getProperty("waqi_api_key");
			} catch (IOException e) {
				throw new RuntimeException("Config.properties not found. Aborting ...");
			}
		}
	}

	/***
	 * Fetching the content from the api and parsing the json result
	 * @param boundingBox
	 */
	public void readJSON(BBox boundingBox) {
		if (aqiApiKey.equals("<WAQI_API_KEY>")){
			throw new RuntimeException("API Key for AQI URL is not found. Aborting...");
		}
		try {
			writeLock.lock();

			URL uri = new URL(url + boundingBox.minLat + "," + boundingBox.minLon + "," + boundingBox.maxLat + "," + boundingBox.maxLon + "&token=" + aqiApiKey);

			HttpURLConnection httpURLConnection = (HttpURLConnection) uri.openConnection();

			int responseCode = httpURLConnection.getResponseCode();

			if (responseCode != 200) {
				throw new RuntimeException("HttpResponseCode: " + responseCode);
			}

			ArrayList<AirQuality> airQualityArrayList = new ArrayList<>();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();

			while ((inputLine = bufferedReader .readLine()) != null) {
				response.append(inputLine);
			}

			bufferedReader.close();

			JSONObject obj = (JSONObject) jsonP.parse(response.toString());
			JSONArray data = (JSONArray) obj.get("data");

			for (Object datum : data) {

				JSONObject obj1 = (JSONObject) datum;

				double lat = (double) obj1.get("lat");
				double lon = (double) obj1.get("lon");
				String aqi = (String) obj1.get("aqi");

				// Regex to check string
				// contains only digits
				String regex = "[0-9]+";

				// Compile the ReGex
				Pattern pattern = Pattern.compile(regex);

				// If the string is empty
				// return false
				if (aqi == null) {
					continue;
				}

				// Find match between given string
				// and regular expression
				// using Pattern.matcher()
				Matcher matcher = pattern.matcher(aqi);

				if (matcher.matches()) {
					double aqiDouble = Double.parseDouble(aqi);
					JSONObject obj2 = (JSONObject) obj1.get("station");
					String name = (String) obj2.get("name");
					airQualityArrayList.add(new AirQuality(lat, lon, aqiDouble, name));
				}
			}

			//assign air quality metric to edge in graphhopper
			Graph gh = hopper.getGraphHopperStorage().getBaseGraph();

			AirQualityBFS airQualityBFS = new AirQualityBFS(hopper, gh, airQualityArrayList);

			airQualityBFS.start(gh.createEdgeExplorer(), 0);
			} 
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			writeLock.unlock();
			System.out.println("WAQI API parsing done...");
		}

	}

	// reading historical csv aqi data
	private void read_historical_aqi(ArrayList<AirQuality> ap) {
		Properties prop=new Properties();
		try(FileInputStream ip = new FileInputStream("config.properties")) {
			prop.load(ip);
			String aqPath=prop.getProperty("air_quality_file");
			BufferedReader br = new BufferedReader(new FileReader(aqPath));
			String newLine;
			String[] strings;
			br.readLine();
			while ((newLine = br.readLine()) != null) {
				strings = newLine.split(",");
				if (strings.length !=0) {
					if (!Objects.equals(strings[0], "") | !Objects.equals(strings[1], "") | !Objects.equals(strings[2], "") | !Objects.equals(strings[3], ""))
						// reads the 1/10/19_av data as aqi
						ap.add(new AirQuality(Double.parseDouble(strings[1]), Double.parseDouble(strings[2]), Double.parseDouble(strings[3]), strings[0]));
				}
			}
			br.close();
		} catch (IOException e) {
			throw new RuntimeException("Path not found");
		}
	}
}