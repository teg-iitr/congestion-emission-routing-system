
var TrafficJamLayer = L.CanvasLayer.extend({
    clear: function() {
        var canvas = this.getCanvas();
        var ctx = canvas.getContext('2d');
        ctx.clearRect(0, 0, canvas.width, canvas.height);
    },
    render: function() {
        var canvas = this.getCanvas();
        var ctx = canvas.getContext('2d');
        // clear canvas
        console.log("render roads");
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        this.renderRoads(ctx);
        // render
    },
    renderRoads: function(ctx) {
        ctx.globalAlpha = 0.6;
        ctx.lineWidth = 4;

        // get center from the map (projected)            
        var obj = roadsJson["roads"];
        if (obj == null)
            return;
        if (obj.lat == null) {
            return;
        }
        for (let i = 0; i < obj.lat.length; i++) {
            ctx.beginPath();
            var speed = obj.speed[i][0];
            if (speed == 0) {
                ctx.lineWidth = 5;
                ctx.strokeStyle = 'gray';

            } else if (speed < 10) {
                ctx.lineWidth = 5;
                ctx.strokeStyle = 'brown';

            } else if (speed < 15) {
                ctx.lineWidth = 4;
                ctx.strokeStyle = 'orange';

            } else if (speed < 20) {
                ctx.lineWidth = 3;
                ctx.strokeStyle = 'yellow';
            } else {
                ctx.lineWidth = 2;
                ctx.strokeStyle = 'green';
            }

            for (let j = 0; j < obj.lat[i].length; j++) {
                var point = this._map.latLngToContainerPoint(new L.latLng(parseFloat(obj.lat[i][j]), parseFloat(obj.lons[i][j])));
                if (j == 0) {
                    ctx.moveTo(point.x, point.y);
                    continue;
                }
                ctx.lineTo(point.x, point.y);
            }
            ctx.stroke();
        }

    }

});
var trafficLayer = new TrafficJamLayer({
    zIndex: 20
});