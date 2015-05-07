/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.person;

import es.jyago.hermes.activityLog.ActivityLog;
import es.jyago.hermes.chart.LineChartInterface;
import es.jyago.hermes.configuration.Configuration;
import es.jyago.hermes.csv.CSVBeanInterface;
import es.jyago.hermes.person.configuration.PersonConfiguration;
import es.jyago.hermes.role.Role;
import es.jyago.hermes.util.Constants;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.io.IOUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.CategoryAxis;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.DateAxis;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 *
 * @author Jorge Yago
 */
@Entity
@Table(name = "person", uniqueConstraints = @UniqueConstraint(columnNames = {"username"}))
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Person.findAll", query = "SELECT p FROM Person p"),
    @NamedQuery(name = "Person.findByPersonId", query = "SELECT p FROM Person p WHERE p.personId = :personId"),
    @NamedQuery(name = "Person.findByFirstName", query = "SELECT p FROM Person p WHERE p.firstName = :firstName"),
    @NamedQuery(name = "Person.findBySurname1", query = "SELECT p FROM Person p WHERE p.surname1 = :surname1"),
    @NamedQuery(name = "Person.findBySurname2", query = "SELECT p FROM Person p WHERE p.surname2 = :surname2"),
    @NamedQuery(name = "Person.findByEmail", query = "SELECT p FROM Person p WHERE p.email = :email"),
    @NamedQuery(name = "Person.findByPhone", query = "SELECT p FROM Person p WHERE p.phone = :phone"),
    @NamedQuery(name = "Person.findByFitbitAccessToken", query = "SELECT p FROM Person p WHERE p.fitbitAccessToken = :fitbitAccessToken"),
    @NamedQuery(name = "Person.findByFitbitAccessTokenSecret", query = "SELECT p FROM Person p WHERE p.fitbitAccessTokenSecret = :fitbitAccessTokenSecret"),
    @NamedQuery(name = "Person.findByComments", query = "SELECT p FROM Person p WHERE p.comments = :comments"),
    @NamedQuery(name = "Person.findByUsernamePassword", query = "SELECT p FROM Person p WHERE p.username = :username AND p.password = :password"),
    @NamedQuery(name = "Person.findByUsername", query = "SELECT p FROM Person p WHERE p.username = :username")})
public class Person implements Serializable, CSVBeanInterface, LineChartInterface {

    private static final Logger log = Logger.getLogger(Person.class.getName());

    public static enum PersonOptions {

        StepsGoal, MinimumSessionMinutes, EndSessionStoppedMinutes, RestStepsThreshold, SessionsPerWeek
    }

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "person_id")
    private Integer personId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "first_name")
    private String firstName;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "surname_1")
    private String surname1;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "surname_2")
    private String surname2;
    // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")//if the field contains email address consider using this annotation to enforce field validation
    @Size(max = 100)
    @Column(name = "email")
    private String email;
    // @Pattern(regexp="^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$", message="Invalid phone/fax format, should be as xxx-xxx-xxxx")//if the field contains phone or fax number consider using this annotation to enforce field validation
    @Size(max = 12)
    @Column(name = "phone")
    private String phone;
    @Lob
    @Column(name = "photo")
    private byte[] photo;
    @Size(max = 10)
    @Column(name = "fitbit_user_id")
    private String fitbitUserId;
    @Size(max = 40)
    @Column(name = "fitbit_access_token")
    private String fitbitAccessToken;
    @Size(max = 40)
    @Column(name = "fitbit_access_token_secret")
    private String fitbitAccessTokenSecret;
    @Size(max = 255)
    @Column(name = "comments")
    private String comments;
    @JoinColumn(name = "role_id", referencedColumnName = "role_id")
    @ManyToOne(optional = false)
    private Role role;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "person", orphanRemoval = true)
    @OrderBy("date ASC")
    private List<ActivityLog> activityLogCollection;
    @Basic(optional = false)
    @Size(min = 1, max = 20)
    @Column(name = "username", unique = true)
    private String username;
    @Basic(optional = false)
    @Size(min = 1, max = 20)
    @Column(name = "password")
    private String password;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "person", orphanRemoval = true)
    private List<PersonConfiguration> configurationCollection;
    @Transient
    private HashMap<String, PersonConfiguration> configurationCollectionHashMap;
    @Transient
    private int thisWeekSessions;

    public Person() {
    }

    public Person(List<Configuration> configList) {
        // Por defecto, la persona tendrá un rol de usuario.
        this.role = new Role(Constants.USER_ROLE);
    }

    // JYFR: Método que será invocado automáticamente tras cargar los datos de la base de datos y de ser inyectados en los atributos correspondientes.
    @PostLoad
    private void init() {
        prepareConfigurationCollectionHashMap();
        calculateSessions();
    }

    private void addConfiguration(Configuration c) {
        PersonConfiguration pc;
        if (c != null) {
            pc = new PersonConfiguration();
            pc.setPerson(this);
            pc.setOption(c);
            pc.setValue(c.getOptionValue());
            configurationCollection.add(pc);
        }
    }

    public Integer getPersonId() {
        return personId;
    }

    public void setPersonId(Integer personId) {
        this.personId = personId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname1() {
        return surname1;
    }

    public void setSurname1(String surname1) {
        this.surname1 = surname1;
    }

    public String getSurname2() {
        return surname2;
    }

    public void setSurname2(String surname2) {
        this.surname2 = surname2;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public void uploadPhoto(FileUploadEvent event) {
        UploadedFile file = event.getFile();

        try {
            photo = IOUtils.toByteArray(file.getInputstream());
        } catch (IOException ex) {
            Logger.getLogger(Person.class.getName()).log(Level.SEVERE, "uploadPhoto() - Error al subir la foto", ex);
        }
    }

    public String getFitbitUserId() {
        return fitbitUserId;
    }

    public void setFitbitUserId(String fitbitUserId) {
        this.fitbitUserId = fitbitUserId;
    }

    public String getFitbitAccessToken() {
        return fitbitAccessToken;
    }

    public void setFitbitAccessToken(String fitbitAccessToken) {
        this.fitbitAccessToken = fitbitAccessToken;
    }

    public String getFitbitAccessTokenSecret() {
        return fitbitAccessTokenSecret;
    }

    public void setFitbitAccessTokenSecret(String fitbitAccessTokenSecret) {
        this.fitbitAccessTokenSecret = fitbitAccessTokenSecret;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<ActivityLog> getActivityLogCollection() {
        return activityLogCollection;
    }

    public boolean isAdmin() {
        return this.role.getRoleId().equals(Constants.ADMINISTRATOR_ROLE);
    }

    public boolean isDoctor() {
        return this.role.getRoleId().equals(Constants.DOCTOR_ROLE);
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.firstName).append(" ").append(this.surname1).append(" ").append(this.surname2);

        return sb.toString();
    }

    private void calculateSessions() {
        thisWeekSessions = 0;

        Calendar cal = Calendar.getInstance();
        int currentWeek = cal.get(Calendar.WEEK_OF_YEAR);

        for (ActivityLog al : activityLogCollection) {
            cal.setTime(al.getDate());
            int week = cal.get(Calendar.WEEK_OF_YEAR);
            if (week == currentWeek) {
                // Vamos sumando las sesiones de cada día de la semana.
                thisWeekSessions += al.getSessionsTotal();
            } else {
                // Como la colección de actividades viene ordenada por fecha descendentemente,
                // cuando sea otra semana podemos salir del bucle.
                break;
            }
        }
    }

    public int getThisWeekSessions() {
        return thisWeekSessions;
    }

    /**
     * Método para obtener los datos de las actividades desde una fecha de
     * inicio a una fecha de fin, agregados según la forma indicada. Si la
     * agregación es nula o un valor no válido, los datos se devolverán por
     * minuto.
     *
     * @param startDate Fecha de inicio (incluida)
     * @param endDate Fecha de fin (incluida)
     * @param aggregation Modo de agregación. Si el valor es nulo o un valor no
     * válido, los datos se devolverán por minuto.
     * @return
     */
    public List<ActivityLog> getActivityLogCollection(Date startDate, Date endDate, String aggregation) {
        List<ActivityLog> filteredList = new ArrayList<>();

        if (startDate == null) {
            startDate = new Date(Long.MIN_VALUE);
        }
        if (endDate == null) {
            endDate = new Date(Long.MAX_VALUE);
        }
        if (activityLogCollection != null) {
            for (ActivityLog activityLog : activityLogCollection) {
                // Comprobamos si la fecha de la actividad está en el rango de fechas que solicita el usuario.
                if ((activityLog.getDate().compareTo(startDate) >= 0) && (activityLog.getDate().compareTo(endDate) <= 0)) {
                    activityLog.setAggregation(aggregation);
                    filteredList.add(activityLog);
                }
            }
        }

        return filteredList;
    }

    public void setActivityLogCollection(List<ActivityLog> activityLogCollection) {
        this.activityLogCollection = activityLogCollection;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<PersonConfiguration> getConfigurationCollection() {
        return configurationCollection;
    }

    public HashMap<String, PersonConfiguration> getConfigurationHashMap() {
        return configurationCollectionHashMap;
    }

    public int getConfigurationIntValue(String key) {
        int value = 0;
        String stringValue = "";

        try {
            stringValue = configurationCollectionHashMap.get(key).getValue();
            value = Integer.parseInt(stringValue);
        } catch (NumberFormatException e) {
            log.log(Level.WARNING, "getConfigurationIntValue() - El valor [{0}] de la clave [{1}] no es un entero. Se devolverá un '0'", new Object[]{stringValue, key});
        }

        return value;
    }

    public void setConfigurationCollection(List<PersonConfiguration> configurationCollection) {
        this.configurationCollection = configurationCollection;
        prepareConfigurationCollectionHashMap();
    }

    public void prepareConfigurationCollectionHashMap() {
        this.configurationCollectionHashMap = new HashMap<>();

        if (configurationCollection != null) {
            for (PersonConfiguration c : configurationCollection) {
                if (c.getOption() != null) {
                    configurationCollectionHashMap.put(c.getOption().getOptionKey(), c);
                }
            }
        }
    }

    public Date getLastSynchronization() {
        if (activityLogCollection != null && !activityLogCollection.isEmpty()) {
            // Al tener la colección de actividades una ordenación ascendente, la actividad más reciente será la que esté en última posición.
            return activityLogCollection.get(activityLogCollection.size()-1).getDate();
        } else {
            return null;
        }
    }

    public boolean hasFitbitCredentials() {
        return (fitbitUserId != null
                && fitbitUserId.length() > 0
                && fitbitAccessToken != null
                && fitbitAccessToken.length() > 0
                && fitbitAccessTokenSecret != null
                && fitbitAccessTokenSecret.length() > 0);
    }

    @Override
    public LineChartModel getLineModel(LinkedHashMap<Date, Integer> values, String title) {
        LineChartModel model = new LineChartModel();
        LineChartSeries series = new LineChartSeries();

        // Rellenamos la serie con las fechas y los totales de pasos.
        for (Date key : values.keySet()) {
            int value = values.get(key);
            series.set(key.getTime(), value);
        }

        // Indicamos el texto de la leyenda.
        series.setLabel(ResourceBundle.getBundle("/Bundle").getString("Steps"));

        model.setTitle(title);
        model.setLegendPosition("ne");
        model.setShowPointLabels(true);
        model.setShowDatatip(true);
        model.setMouseoverHighlight(true);
        model.setDatatipFormat("%1$s -> %2$d");
        model.setSeriesColors("2DC800");
        model.setAnimate(true);
        model.setZoom(true);

        DateAxis xAxis = new DateAxis(ResourceBundle.getBundle("/Bundle").getString("Days"));
        xAxis.setTickAngle(-45);
        xAxis.setTickFormat("%d/%m/%Y");
        model.getAxes().put(AxisType.X, xAxis);

        Axis yAxis = model.getAxis(AxisType.Y);
        yAxis.setLabel(ResourceBundle.getBundle("/Bundle").getString("Steps"));
        yAxis.setMin(0);

        if (!series.getData().isEmpty()) {
            model.addSeries(series);
        }

        return model;
    }

//    public BarChartModel getSessionsBarChartModel(LinkedHashMap<Date, Integer> steps, LinkedHashMap<Date, Integer> continuousSteps, LinkedHashMap<Date, Integer> sessions, String title) {
//        BarChartModel model = new BarChartModel();
//        ChartSeries stepsSeries = new ChartSeries();
//        ChartSeries continuousStepsSeries = new ChartSeries();
//        ChartSeries sessionsSeries = new ChartSeries();
//
//        // TODO: Ver si quitamos los parámetros para coger los datos directamente de la clase.
//        // TODO: Poner/quitar 'interface' para gráficos de barras.
//        // TODO: Si pasamos los 'hashmaps', que tengan el formato necesario para representarlos y así no hay que hacer estos 3 bucles for.
//        // Rellenamos la serie con las fechas y los totales de pasos.
//        for (Map.Entry<Date, Integer> entry : steps.entrySet()) {
//            // FIXME: Cuando arreglen el bug en Primefaces
////            stepsSeries.set(entry.getKey().getTime(), entry.getValue());
//            stepsSeries.set(Constants.df.format(entry.getKey()), entry.getValue());
//        }
//
//        // Rellenamos la serie con las fechas y las sesiones.
//        for (Map.Entry<Date, Integer> entry : sessions.entrySet()) {
//            // FIXME: Cuando arreglen el bug en Primefaces
////            sessionsSeries.set(entry.getKey().getTime(), entry.getValue());
//            sessionsSeries.set(Constants.df.format(entry.getKey()), entry.getValue());
//        }
//
//        // Rellenamos la serie con las fechas y los pasos en continuo de las sesiones.
//        for (Map.Entry<Date, Integer> entry : continuousSteps.entrySet()) {
//            // FIXME: Cuando arreglen el bug en Primefaces
////            continuousStepsSeries.set(entry.getKey().getTime(), entry.getValue());
//            continuousStepsSeries.set(Constants.df.format(entry.getKey()), entry.getValue());
//        }
//
//        // Indicamos el texto de la leyenda.
//        stepsSeries.setLabel(ResourceBundle.getBundle("/Bundle").getString("Steps"));
//        continuousStepsSeries.setLabel(ResourceBundle.getBundle("/Bundle").getString("ContinuousSteps"));
//        sessionsSeries.setLabel(ResourceBundle.getBundle("/Bundle").getString("Sessions"));
//
//        model.setTitle(title);
//        model.setLegendPosition("ne");
//        model.setShowPointLabels(true);
//        model.setShowDatatip(true);
//        model.setMouseoverHighlight(false);
////        model.setDatatipFormat("%1$s -> %2$d");
//        model.setSeriesColors("AEC6CF, AAEEAA, FFB347");
//        model.setAnimate(true);
//        model.setZoom(false);
////        model.setStacked(true);
//
//        // FIXME: No funciona por un bug de Primefaces. Está marcado como registrado el 16 de abril de 2015.
////        DateAxis xAxis = new DateAxis(ResourceBundle.getBundle("/Bundle").getString("Days"));
////        xAxis.setTickAngle(-45);
////        xAxis.setTickFormat("%d/%m/%Y");
////        model.getAxes().put(AxisType.X, xAxis);
//        CategoryAxis xAxis = new CategoryAxis(ResourceBundle.getBundle("/Bundle").getString("Days"));
//        xAxis.setTickAngle(-45);
////        xAxis.setTickFormat("%d/%m/%Y");
//        model.getAxes().put(AxisType.X, xAxis);
//
//        Axis yAxis = model.getAxis(AxisType.Y);
//        yAxis.setLabel(ResourceBundle.getBundle("/Bundle").getString("Steps"));
//        yAxis.setMin(0);
//
//        if (!stepsSeries.getData().isEmpty()) {
//            model.addSeries(stepsSeries);
//        }
//        if (!continuousStepsSeries.getData().isEmpty()) {
//            model.addSeries(continuousStepsSeries);
//        }
//        if (!sessionsSeries.getData().isEmpty()) {
//            model.addSeries(sessionsSeries);
//        }
//
//        return model;
//    }
    
    public BarChartModel getSessionsBarChartModel(LinkedHashMap<String, Integer> activeSessionsSteps, LinkedHashMap<String, Integer> continuousSteps, String title) {
        BarChartModel model = new BarChartModel();
        ChartSeries sessionsStepsSeries = new ChartSeries();
        ChartSeries continuousStepsSeries = new ChartSeries();

        // TODO: Ver si quitamos los parámetros para coger los datos directamente de la clase.
        // TODO: Poner/quitar 'interface' para gráficos de barras.
        // TODO: Si pasamos los 'hashmaps', que tengan el formato necesario para representarlos y así no hay que hacer estos 3 bucles for.
        // Rellenamos la serie con las fechas y los totales de pasos de la sesión.
        for (Map.Entry<String, Integer> entry : activeSessionsSteps.entrySet()) {
            // FIXME: Cuando arreglen el bug en Primefaces
//            stepsSeries.set(entry.getKey().getTime(), entry.getValue());
            sessionsStepsSeries.set(entry.getKey(), entry.getValue());
        }

        // Rellenamos la serie con las fechas y los pasos en continuo de las sesiones.
        for (Map.Entry<String, Integer> entry : continuousSteps.entrySet()) {
            // FIXME: Cuando arreglen el bug en Primefaces
//            continuousStepsSeries.set(entry.getKey().getTime(), entry.getValue());
            continuousStepsSeries.set(entry.getKey(), entry.getValue());
        }

        // Indicamos el texto de la leyenda.
        sessionsStepsSeries.setLabel(ResourceBundle.getBundle("/Bundle").getString("StepsPerSession"));
        continuousStepsSeries.setLabel(ResourceBundle.getBundle("/Bundle").getString("ContinuousSteps"));

        model.setTitle(title);
        model.setLegendPosition("ne");
        model.setShowPointLabels(true);
        model.setShowDatatip(true);
        model.setMouseoverHighlight(false);
//        model.setDatatipFormat("%1$s -> %2$d");
        model.setSeriesColors("AEC6CF, FFB347");
        model.setAnimate(true);
        model.setZoom(false);
//        model.setStacked(true);

        // FIXME: No funciona por un bug de Primefaces. Está marcado como registrado el 16 de abril de 2015.
//        DateAxis xAxis = new DateAxis(ResourceBundle.getBundle("/Bundle").getString("Days"));
//        xAxis.setTickAngle(-45);
//        xAxis.setTickFormat("%d/%m/%Y");
//        model.getAxes().put(AxisType.X, xAxis);
        CategoryAxis xAxis = new CategoryAxis(ResourceBundle.getBundle("/Bundle").getString("Days"));
        xAxis.setTickAngle(90);
//        xAxis.setTickFormat("%d/%m/%Y");
        model.getAxes().put(AxisType.X, xAxis);

        Axis yAxis = model.getAxis(AxisType.Y);
        yAxis.setLabel(ResourceBundle.getBundle("/Bundle").getString("Steps"));
        yAxis.setMin(0);

        if (!sessionsStepsSeries.getData().isEmpty()) {
            model.addSeries(sessionsStepsSeries);
        }
        if (!continuousStepsSeries.getData().isEmpty()) {
            model.addSeries(continuousStepsSeries);
        }

        return model;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (personId != null ? personId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Person)) {
            return false;
        }
        Person other = (Person) object;

        // Dos 'Person' serán iguales si tienen el mismo identificador.
        return !((this.personId == null && other.personId != null) || (this.personId != null && !this.personId.equals(other.personId)));
    }

    @Override
    public String toString() {
        return getFullName();
    }

    @Override
    public CellProcessor[] getProcessors() {
        return new CellProcessor[]{
            new org.supercsv.cellprocessor.constraint.NotNull(), // nombre
            new org.supercsv.cellprocessor.constraint.NotNull(), // apellido1
            new org.supercsv.cellprocessor.constraint.NotNull(), // apellido2
            new org.supercsv.cellprocessor.Optional(), // email
            new org.supercsv.cellprocessor.Optional(), // telefono
            new org.supercsv.cellprocessor.Optional(), // observaciones
        };
    }

    @Override
    public String[] getFields() {
        return new String[]{"firstName", "surname1", "surname2", "email", "phone", "comments"};
    }
}
