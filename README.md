# gh-configured-router
This is a real-time traffic and air pollution monitoring and routing web application which relies on Spring Boot, Maven and various web apis. 
The routing is done using the existing graphhopper libraries. Traffic data and air pollution data is fetched every 10 minutes from Here Maps Api and Aqicn Api and real-time routing is done. 
In order to use the above project,
* Clone the repository using the following command on your console/command prompt in the location of your choice: <br>git clone https://github.com/teg-iitr/gh_configured_router.git 
* Go to the location of your project and execute the following command to run project and also set the api keys:<br> mvn spring-boot:run -Dspring-boot.run.arguments=--here_api_key=<YOUR_HERE_API_KEY>,--waqi_api_key=<YOUR_WAQI_API_KEY>,--datareader.file=<LOCATION_OSM.PBF_FILE>
* Open http://localhost:9098/ where the website will be displayed
* After doing the routing, to get json response of the routing, add "&mediaType=json" as another parameter. For example: http://localhost:9098/routing?StartLoc=77.09652%2C28.555764&EndLoc=77.32%2C28.57&RouteType=fastest&Vehicle=bike&mediaType=json
* In order to run the project without any additional commandline arguments, simply type  mvn spring-boot:run  

