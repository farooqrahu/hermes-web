package es.jyago.hermes.activityLog;

import es.jyago.hermes.csv.ICSVBean;
import es.jyago.hermes.util.Constants;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ift.CellProcessor;

public class ActivityLogCSV implements ICSVBean {

    private Date date;
    private int totalSteps;
    private Date sessionStartTime;
    private Date sessionEndTime;
    private int sessionSteps;
    private Date restStartTime;
    private Date restEndTime;

    protected CellProcessor[] cellProcessors;
    protected String[] fields;
    protected String[] headers;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getFormattedDate() {
        return Constants.df.format(date);
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }

    public Date getSessionStartTime() {
        return sessionStartTime;
    }

    public String getFormattedSessionStartTime() {
        return sessionStartTime != null ? Constants.dfTime.format(sessionStartTime) : "";
    }

    public void setSessionStartTime(Date sessionStartTime) {
        this.sessionStartTime = sessionStartTime;
    }

    public Date getSessionEndTime() {
        return sessionEndTime;
    }

    public String getFormattedSessionEndTime() {
        return sessionEndTime != null ? Constants.dfTime.format(sessionEndTime) : "";
    }

    public void setSessionEndTime(Date sessionEndTime) {
        this.sessionEndTime = sessionEndTime;
    }

    public int getSessionSteps() {
        return sessionSteps;
    }

    public void setSessionSteps(int sessionSteps) {
        this.sessionSteps = sessionSteps;
    }

    public Date getRestStartTime() {
        return restStartTime;
    }

    public String getFormattedRestStartTime() {
        return restStartTime != null ? Constants.dfTime.format(restStartTime) : "";
    }

    public void setRestStartTime(Date restStartTime) {
        this.restStartTime = restStartTime;
    }

    public Date getRestEndTime() {
        return restEndTime;
    }

    public String getFormattedRestEndTime() {
        return restEndTime != null ? Constants.dfTime.format(restEndTime) : "";
    }

    public void setRestEndTime(Date restEndTime) {
        this.restEndTime = restEndTime;
    }

    @Override
    public CellProcessor[] getProcessors() {
        return cellProcessors;
    }

    @Override
    public String[] getFields() {
        return fields;
    }

    @Override
    public String[] getHeaders() {
        return headers;
    }

    @Override
    public void init(Integer columns) {
        // Si el número de columnas es 'null', se establecen las que se definan internamente en este método.

        List<CellProcessor> cpl = new ArrayList<>();

        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull()); // fecha de la actividad
        cpl.add(new org.supercsv.cellprocessor.constraint.NotNull(new ParseInt())); // total de pasos
        cpl.add(new org.supercsv.cellprocessor.Optional()); // hora de inicio de la sesión
        cpl.add(new org.supercsv.cellprocessor.Optional()); // hora de fin de la sesión
        cpl.add(new org.supercsv.cellprocessor.Optional(new ParseInt())); // pasos de la sesión
        cpl.add(new org.supercsv.cellprocessor.Optional()); // hora de inicio de la parada
        cpl.add(new org.supercsv.cellprocessor.Optional()); // hora de fin de la parada

        if (columns != null) {
            cellProcessors = new CellProcessor[columns];
            for (int i = 0; i < columns; i++) {
                cellProcessors[i] = cpl.get(i);
            }
        } else {
            cellProcessors = cpl.toArray(new CellProcessor[cpl.size()]);
        }

        List<String> f = new ArrayList();

        f.add("formattedDate");
        f.add("totalSteps");
        f.add("formattedSessionStartTime");
        f.add("formattedSessionEndTime");
        f.add("sessionSteps");
        f.add("formattedRestStartTime");
        f.add("formattedRestEndTime");

        if (columns != null) {
            fields = new String[columns];
            for (int i = 0; i < columns; i++) {
                fields[i] = f.get(i);
            }
        } else {
            fields = f.toArray(new String[f.size()]);
        }

        List<String> h = new ArrayList();
        ResourceBundle bundle = ResourceBundle.getBundle("/Bundle");

        h.add(bundle.getString("Date"));
        h.add(bundle.getString("TotalSteps"));
        h.add(bundle.getString("SessionStartTime"));
        h.add(bundle.getString("SessionEndTime"));
        h.add(bundle.getString("SessionTotalSteps"));
        h.add(bundle.getString("RestStartTime"));
        h.add(bundle.getString("RestEndTime"));

        if (columns != null) {
            headers = new String[columns];
            for (int i = 0; i < columns; i++) {
                headers[i] = h.get(i);
            }
        } else {
            headers = h.toArray(new String[h.size()]);
        }
    }
}
