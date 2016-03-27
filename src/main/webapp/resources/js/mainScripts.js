function userSleepBarChartExtender() {
    this.cfg.axes.xaxis = {
        ticks: ['0', '1', '2'],
        showTicks: false
    };
    this.cfg.axes.yaxis = {
        tickOptions: {
            formatString: '%.1f'
        }
    };
}

function userStepsLineChartExtender() {
    this.cfg.axes.xaxis = {
        showTicks: false
    };
    this.cfg.highlighter = {
        show: true,
        tooltipLocation: 'ne',
        tooltipContentEditor: function (str, seriesIndex, pointIndex, plot) {
            var parts = str.split(",");
            var miliseconds = parts[0];
            var seconds = parseInt((miliseconds / 1000) % 60);
            var minutes = parseInt((miliseconds / (1000 * 60)) % 60);
            var hours = parseInt((miliseconds / (1000 * 60 * 60)) % 24);
            var fhours = (hours > 9) ? hours : ('0' + hours);
            var fminutes = (minutes > 9) ? minutes : ('0' + minutes);
            var fseconds = (seconds > 9) ? seconds : ('0' + seconds);
            return fhours + ":" + fminutes + ":" + fseconds + " -> " + parts[1];
        }
    };
}

function pieExtender() {
    this.cfg.highlighter = {
        show: true,
        tooltipLocation: 'ne',
        useAxesFormatters: false,
        formatString: '%s = %d%'
    };
}
// FIXME: ¿Actualizar los gráficos con el cambio de tamaño de la ventana del navegador?
//                window.onresize = function (event) {
//                    updatechartpanel();
//                }