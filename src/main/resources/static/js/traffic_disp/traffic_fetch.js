/*
Making an ajax call request to fetch traffic information from backend to be fetched andr rendered every 10 minutes
*/
roadsJson = {};
function traffic_info() {
    var checkbox = document.getElementById("traffic_disp");
    if (checkbox.checked == false) {
        map.removeLayer(trafficLayer);
    } else {

        trafficLayer.addTo(map);
        $.ajax({
            type: "GET",
            url: "/traffic",
            dataType: "json",
            success: function(data) {
                console.log(data);
                roadsJson["roads"] = data;
                trafficLayer.redraw();
                setTimeout(function() {
                    traffic_info();
                }, 60 *60* 1000);
            }
        })
    }
}