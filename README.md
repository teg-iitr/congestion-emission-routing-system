# gh-configured-router
This is a real-time traffic and air pollution monitoring and routing web application which relies on Spring Boot, Maven and various web apis. 
The routing is done using the existing graphhopper libraries. Traffic data and air pollution data is fetched every 10 minutes from Here Maps Api and Aqicn Api and real-time routing is done. 
In order to use the above project,
* Clone the repository using the following command on your console/command prompt in the location of your choice: <br>git clone https://github.com/teg-iitr/gh_configured_router.git 
* Open Eclipse IDE for Enterprise Java and Web Developers and go to Files-> Open Projects From File System and select the repository location
* Right click on gh_configured_router->src/main/java->com.map.app-> AppApplication.java and click on "Run as application"
* The website can be accessed at localhost port number 9098 (http://localhost:9098/). <br>
Note: Acquire API token for Graphhopper and Here Maps Api usage. <br>
Go to gh_configured_router->src/main/java->com.map.app.dto-> routePathDto.java and set variable apiKey you graphhopper Api key(line 22).<br> Similarily, go to gh_configured_router->src/main/java->com.map.app.dto-> TrafficdatDto.java and add api token in the string variable URL is assigned to(43 line).

