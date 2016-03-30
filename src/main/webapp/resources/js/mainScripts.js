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