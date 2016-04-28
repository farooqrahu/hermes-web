package es.jyago.hermes;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Report implements Serializable {

    private static final long serialVersionUID = 1L;
    private String fileName;
    private String url;
    private String description;

    public Report() {
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 29).
                append(url).
                toHashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Report)) {
            return false;
        }
        Report other = (Report) object;
        if ((this.url == null && other.url != null) || (this.url != null && !this.url.equals(other.url))) {
            return false;
        }

        return new EqualsBuilder().
                append(url, other.url).
                isEquals();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.url);

        return sb.toString();
    }

}
