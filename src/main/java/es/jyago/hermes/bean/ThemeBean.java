package es.jyago.hermes.bean;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import javax.faces.event.AjaxBehaviorEvent;

import org.primefaces.component.themeswitcher.ThemeSwitcher;

public class ThemeBean implements Serializable {

    // Temas visuales de PrimeFaces.
    private static Map<String, String> themes;

    private String themeName;

    public ThemeBean() {
        // Tema visual por defecto.
        this("pepper-grinder");
    }

    public ThemeBean(String themeName) {
        super();
        this.themeName = themeName;
        fillThemes();
    }

    public Map<String, String> getThemes() {
        return themes;
    }

    public String getThemeName() {
        return themeName;
    }

    public void setTheme(String themeName) {
        this.themeName = themeName;
    }

    private void fillThemes() {
        themes = new TreeMap();
        themes.put("Aristo", "aristo");
        themes.put("Black-Tie", "black-tie");
        themes.put("Blitzer", "blitzer");
        themes.put("Bluesky", "bluesky");
        themes.put("Casablanca", "casablanca");
        themes.put("Cupertino", "cupertino");
        themes.put("Dark-Hive", "dark-hive");
        themes.put("Dot-Luv", "dot-luv");
        themes.put("Eggplant", "eggplant");
        themes.put("Excite-Bike", "excite-bike");
        themes.put("Flick", "flick");
        themes.put("Glass-X", "glass-x");
        themes.put("Hot-Sneaks", "hot-sneaks");
        themes.put("Humanity", "humanity");
        themes.put("Le-Frog", "le-frog");
        themes.put("Midnight", "midnight");
        themes.put("Mint-Choc", "mint-choc");
        themes.put("Overcast", "overcast");
        themes.put("Pepper-Grinder", "pepper-grinder");
        themes.put("Redmond", "redmond");
        themes.put("Rocket", "rocket");
        themes.put("Sam", "sam");
        themes.put("Smoothness", "smoothness");
        themes.put("South-Street", "south-street");
        themes.put("Start", "start");
        themes.put("Sunny", "sunny");
        themes.put("Swanky-Purse", "swanky-purse");
        themes.put("Trontastic", "trontastic");
        themes.put("UI-Darkness", "ui-darkness");
        themes.put("UI-Lightness", "ui-lightness");
        themes.put("Vader", "vader");
    }

    public void saveTheme(AjaxBehaviorEvent ajax) {
        setTheme((String) ((ThemeSwitcher) ajax.getSource()).getValue());
    }
}
