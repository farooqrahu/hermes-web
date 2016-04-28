package es.jyago.hermes.util;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.annotation.PostConstruct;


@Named(value = "templateBean")
@SessionScoped
public class TemplateBean implements Serializable {

    private String page;

    public TemplateBean() {
    }

    @PostConstruct
    public void init() {
        this.page = "/login.xhtml";
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

}
