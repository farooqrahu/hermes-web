/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

/**
 *
 * @author Jorge Yago
 */
@ManagedBean
public class GalleryBean {

    private List<String> images;

    @ManagedProperty("#{bundle}")
    private ResourceBundle bundle;

    @PostConstruct
    public void init() {
        images = new ArrayList();
        for (int i = 1; i <= 8; i++) {
            images.add(i + ".png");
        }
    }

    public List<String> getImages() {
        return images;
    }

    public String title(String image) {
        String title;

        try {
            title = bundle.getString("Imagen" + image);
        } catch (MissingResourceException ex) {
            title = bundle.getString("sin_descripcion");
        }

        return title;
    }

    public void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }
}
