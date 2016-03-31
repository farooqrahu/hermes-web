function stepsSessionsChartExtender() {
    this.cfg.axes.xaxis = {
        showTicks: false
    };
    this.cfg.highlighter = {
        show: true,
        tooltipLocation: 'ne',
        tooltipContentEditor: function (str, seriesIndex, pointIndex, plot) {
            var parts = str.split(",");
            var milliseconds = parts[0];
            var seconds = parseInt((milliseconds / 1000) % 60);
            var minutes = parseInt((milliseconds / (1000 * 60)) % 60);
            var hours = parseInt((milliseconds / (1000 * 60 * 60)) % 24);
            var fhours = (hours > 9) ? hours : ('0' + hours);
            var fminutes = (minutes > 9) ? minutes : ('0' + minutes);
            var fseconds = (seconds > 9) ? seconds : ('0' + seconds);
            var ftime = fhours + ":" + fminutes + ":" + fseconds;
            switch (seriesIndex) {
                case 1:
                    return parts[1] > 0 ? ftime + " -> " + "En sesión" : ftime + " -> " + parts[1];
                    break;
                case 2:
                    return parts[1] > 0 ? ftime + " -> " + "Parada" : ftime + " -> " + parts[1];
                    break;
                default:
                    return  ftime + " -> " + parts[1];
            }
        }
    };
    this.cfg.seriesDefaults = {
        lineWidth: 2, 
        markerOptions: {size: 2}
    };
}

function monthStepsChartExtender() {
    this.cfg.axes.yaxis = {
        showTicks: false
    };
}

function monthSessionsChartExtender() {
    this.cfg.axes.yaxis = {
        showTicks: false
    };
}
