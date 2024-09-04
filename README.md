# congestion-emission-routing-system
This is a real-time traffic and air pollution monitoring and routing web application which relies on Spring Boot, Maven and various web apis. 
The routing is done using the existing graphhopper libraries. Traffic data and air pollution data is fetched every 10 minutes from Here Maps Api and Aqicn Api and real-time routing is done.<br>
# Notes
* Used the following geocoding library: https://github.com/location-iq/leaflet-geocoder <br>
# Steps
In order to use the above project,
* Clone the repository using the following command on your console/command prompt in the location of your choice: <br>git clone https://github.com/teg-iitr/congestion-emission-routing-system.git 
* Get your two API keys from:
  * HERE Maps REST API (freemium): https://developer.here.com
  * WAQI API: https://aqicn.org/api/
* Paste your API keys to config.properties in the relevant location
* Go to **Run** from the menu bar, then goto **Edit Configurations...**, then add a new Maven configuration (it will automatically select your project), then in Run command location paste: **spring-boot:run** and apply
* Run your **gh_configured_router** 
* Go to the location of your project and execute the following command to run project and also set the api keys:<br> mvn spring-boot:run -Dspring-boot.run.arguments=--here_api_key=<YOUR_HERE_API_KEY>,--waqi_api_key=<YOUR_WAQI_API_KEY>,--datareader.file=<LOCATION_OSM.PBF_FILE>
* Open http://localhost:9098/ where the website will be displayed
* After doing the routing, to get json response of the routing, add "&mediaType=json" as another parameter. For example: http://localhost:9098/routing?StartLoc=77.09652%2C28.555764&EndLoc=77.32%2C28.57&RouteType=fastest&Vehicle=bike&mediaType=json
* In order to run the project without any additional commandline arguments, simply type  mvn spring-boot:run  <br>
Used the following geocoding library: https://github.com/location-iq/leaflet-geocoder<br>
Please note that specifying points outside the bounds of the datareader file currently raises PointOutOfBoundsException error:<br>
[com.graphhopper.util.exceptions.PointOutOfBoundsException: Point 0 is out of bounds: your_latitude,your_longitude the bounds are: bbox of datareader file] <br>
We are currently working on giving a relevant message to the client instead.