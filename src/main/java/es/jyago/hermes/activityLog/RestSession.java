package es.jyago.hermes.activityLog;

import es.jyago.hermes.util.Constants;
import java.util.Date;

public class RestSession {

    private Date startDate;
    private Date endDate;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getFormattedStartDate() {
        return Constants.dfTime.format(startDate);
    }

    public String getFormattedEndDate() {
        return Constants.dfTime.format(endDate);
    }
}
