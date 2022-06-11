package com.map.app;

import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.util.EdgeIteratorState;
import com.map.app.containers.AirQualityDataExtractor;
import com.map.app.containers.RoutePathContainer;
import com.map.app.containers.TrafficDataExtractor;
import com.map.app.containers.UrlTransformer;
import com.map.app.model.UrlContainer;
import com.map.app.service.TrafficAndRoutingService;
import com.map.app.service.TransportMode;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SensitivityAnalysis {
    public static void main(String[] args) {

        TrafficAndRoutingService trafficAndRoutingService = new TrafficAndRoutingService();
        double pollutionFactor, timeFactor;
        trafficAndRoutingService.start();
//        generateTrafficData(trafficAndRoutingService);
//        generateAQIData(trafficAndRoutingService);
        readTrafficData(trafficAndRoutingService);
//        for (timeFactor = 0.0; timeFactor <= 1.0; timeFactor += 0.10) {
//            for (pollutionFactor = 0.0; pollutionFactor <= 1.0; pollutionFactor += 0.10) {
////                System.out.println((double) Math.round(timeFactor * 100) / 100);
////                System.out.println((double) Math.round(pollutionFactor * 100) / 100);
//                trafficAndRoutingService.start();
//                UrlTransformer urlTransformer = new UrlTransformer();
//                urlTransformer.setStartLoc("77.2060561,28.7152338");
//                urlTransformer.setEndLoc("77.1953703,28.5741575");
//                urlTransformer.setRouteType("balanced");
//                urlTransformer.setVehicle("car");
//                UrlContainer urlContainer = urlTransformer.convert();
//                RoutePathContainer routePathContainer = trafficAndRoutingService.getRoutePathContainer();
//                routePathContainer.setGetTimeFactor((double) Math.round(timeFactor * 100) / 100);
//                routePathContainer.setGetPollutionFactor((double) Math.round(pollutionFactor * 100) / 100);
//                routePathContainer.finalPath(urlContainer, "balanced_car", TransportMode.car);
//                System.out.println("end of iteration");
//            }
//        }
    }

    private static void readTrafficData(TrafficAndRoutingService trafficAndRoutingService) {
        TrafficDataExtractor trafficDataExtractor = trafficAndRoutingService.getTrafficDataExtractor();
        AllEdgesIterator allEdges = trafficDataExtractor.getHopper().getGraphHopperStorage().getBaseGraph().getAllEdges();
        FlagEncoder encoder = trafficDataExtractor.getHopper().getEncodingManager().getEncoder("car");
        DecimalEncodedValue avgTimeEnc = encoder.getDecimalEncodedValue("time");
        DecimalEncodedValue avgSpeedEnc = encoder.getAverageSpeedEnc();
        Map<Integer, List<Double>> trafficData = readResults("here_map_output", "here_map.csv");
        System.out.println(trafficData);
    }

    private static  Map<Integer, List<Double>> readResults(String dirName, String fileName) {
        Map<Integer, List<Double>> values = new HashMap<>();
        try {
            Properties prop = new Properties();
            String inputDir;
            try (FileInputStream ip = new FileInputStream("config.properties")) {
                prop.load(ip);
                inputDir = prop.getProperty(dirName);
            } catch (IOException e) {
                throw new RuntimeException("Config properties are not found. Aborting ...");
            }
            BufferedReader reader = new BufferedReader(new FileReader(inputDir + fileName));
            String line = "";
            int count = 0;
            reader.readLine();
            while (reader.readLine() != null) {
                count++;
            }
            reader.reset();
            reader.readLine();
            for (int i = 0; i < count; i++) {
                while ((line = reader.readLine()) != null) {
                    values.put(i, Stream.of(line.split(","))
                                    .mapToDouble(Double::parseDouble)
                                    .boxed()
                                    .collect(Collectors.toCollection(ArrayList<Double>::new)));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return values;
    }

    private static void generateAQIData(TrafficAndRoutingService trafficAndRoutingService) {
        AirQualityDataExtractor airQualityDataExtractor = trafficAndRoutingService.getAirQualityDataExtractor();
        AllEdgesIterator allEdges = airQualityDataExtractor.getHopper().getGraphHopperStorage().getBaseGraph().getAllEdges();
        initializeCSV("aqi_data_output", "aqi_data.csv", "edge", "smoke", "");
        FlagEncoder encoder = airQualityDataExtractor.getHopper().getEncodingManager().getEncoder("car");
        DecimalEncodedValue smokeEnc = encoder.getDecimalEncodedValue("smoke");
        while (allEdges.next()) {
            int adjNode = allEdges.getAdjNode();
            int edgeId = allEdges.getEdge();
            EdgeIteratorState edgeIterator = airQualityDataExtractor.getHopper().getGraphHopperStorage().getBaseGraph().getEdgeIteratorState(edgeId, adjNode);
            writeResults("aqi_data_output", "aqi_data.csv", String.valueOf(edgeId), String.valueOf(edgeIterator.get(smokeEnc)), "");
        }
    }

    private static void generateTrafficData(TrafficAndRoutingService trafficAndRoutingService) {
        TrafficDataExtractor trafficDataExtractor = trafficAndRoutingService.getTrafficDataExtractor();
        AllEdgesIterator allEdges = trafficDataExtractor.getHopper().getGraphHopperStorage().getBaseGraph().getAllEdges();
        initializeCSV("here_map_output", "here_map.csv", "edge", "average_speed", "travel_time");
        FlagEncoder encoder = trafficDataExtractor.getHopper().getEncodingManager().getEncoder("car");
        DecimalEncodedValue avgTimeEnc = encoder.getDecimalEncodedValue("time");
        DecimalEncodedValue avgSpeedEnc = encoder.getAverageSpeedEnc();
        while (allEdges.next()) {
            int adjNode = allEdges.getAdjNode();
            int edgeId = allEdges.getEdge();
            EdgeIteratorState edgeIterator = trafficDataExtractor.getHopper().getGraphHopperStorage().getBaseGraph().getEdgeIteratorState(edgeId, adjNode);
            writeResults("here_map_output", "here_map.csv", String.valueOf(edgeId), String.valueOf(edgeIterator.get(avgSpeedEnc)), String.valueOf(edgeIterator.get(avgTimeEnc)));
        }
    }


    private static void writeResults(String dir_name, String file_name, String value1, String value2, String value3) {
        FileWriter csvwriter;
        BufferedWriter bufferedWriter = null;
        try {
            Properties prop = new Properties();
            String outputDir;
            try (FileInputStream ip = new FileInputStream("config.properties")) {
                prop.load(ip);
                outputDir = prop.getProperty(dir_name);
            } catch (IOException e) {
                throw new RuntimeException("Config properties are not found. Aborting ...");
            }
            csvwriter = new FileWriter(outputDir + file_name, true);
            bufferedWriter = new BufferedWriter(csvwriter);
            StringJoiner stringJoiner = new StringJoiner(",");
            stringJoiner
                    .add(value1)
                    .add(value2)
                    .add(value3);
            bufferedWriter.write(stringJoiner.toString());
            bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert bufferedWriter != null;
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void initializeCSV(String dir_name, String file_name, String column1, String column2, String column3) {
        FileWriter csvwriter;
        BufferedWriter bufferedWriter = null;
        try {
            Properties prop = new Properties();
            String outputDir;

            try (FileInputStream ip = new FileInputStream("config.properties")) {
                prop.load(ip);
                outputDir = prop.getProperty(dir_name);
            } catch (IOException e) {
                throw new RuntimeException("Config properties are not found. Aborting ...");
            }

            csvwriter = new FileWriter(outputDir + file_name, false);
            bufferedWriter = new BufferedWriter(csvwriter);
            StringJoiner stringJoiner = new StringJoiner(",");
            stringJoiner
                    .add(column1)
                    .add(column2)
                    .add(column3);
            bufferedWriter.write(stringJoiner.toString());
            bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert bufferedWriter != null;
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
