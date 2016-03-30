function stepsChartExtender() {
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
            if (seriesIndex > 0)
                return parts[1] > 0 ? ftime + " -> " + "En sesión" :  ftime + " -> " + "Inactivo";
            else
                return  ftime + " -> " + parts[1];
        }
    };
    this.cfg.seriesDefaults = {
        markerOptions: {size: 2}
    };
}