function customSleepMonthChartExtender() {
    this.cfg.seriesDefaults = {
        rendererOptions: {
            barPadding: 1,
            barMargin: 1,
            barWidth: 10,
        }
    };
    this.cfg.axes.yaxis = {
        tickOptions: {
            formatString: '%.1f'
        }
    };
    this.cfg.highlighter = {
        show: true,
        tooltipContentEditor: function (str, seriesIndex, pointIndex, plot) {
            var sh = plot.data[3][pointIndex];
            var sm = plot.data[4][pointIndex];
            var eh = plot.data[5][pointIndex];
            var em = plot.data[6][pointIndex];
            var shf = (sh > 9) ? sh : ('0' + sh);
            var smf = (sm > 9) ? sm : ('0' + sm);
            var ehf = (eh > 9) ? eh : ('0' + eh);
            var emf = (em > 9) ? em : ('0' + em);
            var html = "(" + shf + ":" + smf + " - " + ehf + ":" + emf + ") " + (str.split(","))[1];
            setStartTime(shf, smf);
            setEndTime(ehf, emf);
            return html;
        }
    };
    this.cfg.series[3] = {
        show: false,
        label: '',
        showLine: false,
        showMarker: false
    }
    this.cfg.series[4] = {
        show: false,
        label: '',
        showLine: false,
        showMarker: false
    }
    this.cfg.series[5] = {
        show: false,
        label: '',
        showLine: false,
        showMarker: false
    }
    this.cfg.series[6] = {
        show: false,
        label: '',
        showLine: false,
        showMarker: false
    }
}
function setStartTime(hours, minutes) {

    var h, m;
    h = 30 * ((hours % 12) + minutes / 60);
    m = 6 * minutes;

    document.getElementById('sh_pointer').setAttribute('transform', 'rotate(' + h + ', 50, 50)');
    document.getElementById('sm_pointer').setAttribute('transform', 'rotate(' + m + ', 50, 50)');
    document.getElementById('stime').innerHTML = hours + ':' + minutes;
}
function setEndTime(hours, minutes) {

    var h, m;
    h = 30 * ((hours % 12) + minutes / 60);
    m = 6 * minutes;

    document.getElementById('eh_pointer').setAttribute('transform', 'rotate(' + h + ', 50, 50)');
    document.getElementById('em_pointer').setAttribute('transform', 'rotate(' + m + ', 50, 50)');
    document.getElementById('etime').innerHTML = hours + ':' + minutes;
}