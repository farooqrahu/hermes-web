package es.jyago.hermes.person;

import es.jyago.hermes.contextLog.ContextLog;
import es.jyago.hermes.activityLog.ActivityLog;
import es.jyago.hermes.configuration.Configuration;
import es.jyago.hermes.healthLog.HealthLog;
import es.jyago.hermes.person.configuration.PersonConfiguration;
import es.jyago.hermes.location.LocationLog;
import es.jyago.hermes.role.Role;
import es.jyago.hermes.alert.Alert;
import es.jyago.hermes.bean.ThemeBean;
import es.jyago.hermes.sleepLog.SleepLog;
import es.jyago.hermes.util.Constants;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.LocalDate;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

@Entity
@Table(name = "person", uniqueConstraints = @UniqueConstraint(columnNames = {"username"}))
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Person.findAll", query = "SELECT p FROM Person p"),
    @NamedQuery(name = "Person.findByPersonId", query = "SELECT p FROM Person p WHERE p.personId = :personId"),
    @NamedQuery(name = "Person.findByFullName", query = "SELECT p FROM Person p WHERE p.fullName = :fullName"),
    @NamedQuery(name = "Person.findByEmail", query = "SELECT p FROM Person p WHERE p.email = :email"),
    @NamedQuery(name = "Person.findByPhone", query = "SELECT p FROM Person p WHERE p.phone = :phone"),
    @NamedQuery(name = "Person.findByFitbitAccessToken", query = "SELECT p FROM Person p WHERE p.fitbitAccessToken = :fitbitAccessToken"),
    @NamedQuery(name = "Person.findByFitbitRefreshToken", query = "SELECT p FROM Person p WHERE p.fitbitRefreshToken = :fitbitRefreshToken"),
    @NamedQuery(name = "Person.findByComments", query = "SELECT p FROM Person p WHERE p.comments = :comments"),
    @NamedQuery(name = "Person.findByUsernamePassword", query = "SELECT p FROM Person p WHERE p.username = :username AND p.password = :password"),
    @NamedQuery(name = "Person.findByUsername", query = "SELECT p FROM Person p WHERE p.username = :username")})
public class Person implements Serializable {

    private static final Logger LOG = Logger.getLogger(Person.class.getName());

    private static final long serialVersionUID = 1L;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "person", orphanRemoval = true)
    @OrderBy("dateLog ASC")
    private List<ActivityLog> activityLogList;
    @Transient
    private TreeMap<Date, ActivityLog> activityLogMap;
    @Transient
    private boolean alertIfUnableToSynchronize;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "person")
    private List<Alert> alertList;
    @Size(max = 50)
    @Column(name = "city")
    private String city;
    @Size(max = 255)
    @Column(name = "comments")
    private String comments;
    @Transient
    private HashMap<String, PersonConfiguration> configurationHashMap;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "person", orphanRemoval = true)
    private List<PersonConfiguration> configurationList;
    @Size(max = 50)
    @Column(name = "country")
    private String country;
    // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")//if the field contains email address consider using this annotation to enforce field validation
    @Size(max = 100)
    @Column(name = "email")
    private String email;
    @Size(max = 300)
    @Column(name = "fitbit_access_token")
    private String fitbitAccessToken;
    @Size(max = 100)
    @Column(name = "fitbit_refresh_token")
    private String fitbitRefreshToken;
    @Size(max = 10)
    @Column(name = "fitbit_user_id")
    private String fitbitUserId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "full_name")
    private String fullName;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "person")
    private List<HealthLog> healthLogList;
    @Transient
    private TreeMap<Date, HealthLog> healthLogMap;
    @Size(max = 20)
    @Column(name = "ip")
    private String ip;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "person", orphanRemoval = true)
    @OrderBy("dateLog ASC")
    private List<LocationLog> locationLogList;
    @Basic(optional = false)
    @Size(min = 1, max = 20)
    @Column(name = "password")
    private String password;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "person_id")
    private Integer personId;
    // @Pattern(regexp="^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$", message="Invalid phone/fax format, should be as xxx-xxx-xxxx")//if the field contains phone or fax number consider using this annotation to enforce field validation
    @Size(max = 12)
    @Column(name = "phone")
    private String phone;
    @Lob
    @Column(name = "photo")
    private byte[] photo;
    @Size(max = 50)
    @Column(name = "region")
    private String region;
    @NotNull
    @Column(name = "registration_date")
    @Temporal(TemporalType.DATE)
    private Date registrationDate;

    @JoinColumn(name = "role_id", referencedColumnName = "role_id")
    @ManyToOne(optional = false)
    private Role role;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "person", orphanRemoval = true)
    @OrderBy("dateLog ASC")
    private List<SleepLog> sleepLogList;
    @Transient
    private TreeMap<Date, SleepLog> sleepLogMap;
    @Transient
    private int thisWeekSessions;
    @Basic(optional = false)
    @Size(min = 1, max = 20)
    @Column(name = "username", unique = true)
    private String username;
    @Transient
    private Date alertNotificationsTime;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "person", orphanRemoval = true)
    @OrderBy("dateLog ASC")
    private List<ContextLog> contextLogList;
    @Transient
    private TreeMap<Date, ContextLog> contextLogMap;
    @Transient
    private ThemeBean theme;
    @Size(max = 64)
    @Column(name = "sha")
    private String sha;

    @Transient
    private String daysReceivedFromFitbit;
    @Transient
    private String daysSentToZtreamy;

    public Person() {
        super();
        this.registrationDate = new Date();
        this.configurationList = new ArrayList<>();
        this.activityLogList = new ArrayList<>();
        this.sleepLogList = new ArrayList<>();
        this.healthLogList = new ArrayList<>();
        // Por defecto, la persona tendrá un rol de usuario.
        this.role = new Role(Constants.USER_ROLE);
    }

    public void addActivityLogList(List<ActivityLog> activityLogList) {
        if (activityLogList != null && !activityLogList.isEmpty()) {
            if (activityLogMap == null) {
                prepareActivityLogMap();
            }

            for (ActivityLog activityLog : activityLogList) {
                ActivityLog old = activityLogMap.remove(activityLog.getDateLog());
                if (old != null) {
                    activityLog.setActivityLogId(old.getActivityLogId());
                }
                activityLogMap.put(activityLog.getDateLog(), activityLog);
            }

            this.activityLogList = new ArrayList<>(activityLogMap.values());
        }
    }

    public void addHealthLogList(List<HealthLog> healthLogList) {
        if (healthLogList != null && !healthLogList.isEmpty()) {
            if (healthLogMap == null) {
                prepareHealthLogMap();
            }

            for (HealthLog healthLog : healthLogList) {
                HealthLog old = healthLogMap.remove(healthLog.getDateLog());
                if (old != null) {
                    healthLog.setHealthLogId(old.getHealthLogId());
                }
                healthLogMap.put(healthLog.getDateLog(), healthLog);
            }

            this.healthLogList = new ArrayList<>(healthLogMap.values());
        }
    }

    public void addSleepLogList(List<SleepLog> sleepLogList) {
        // En el caso del sueño, el registro se va modificando conforme avanza el día (la madrugada o la franja en la que duerme el usuario),
        // por tanto, comparamos el registro obtenido de Fitbit con el que tenemos registrado y si son iguales, no se hace nada.
        if (sleepLogList != null && !sleepLogList.isEmpty()) {
            if (sleepLogMap == null) {
                prepareSleepLogMap();
            }

            for (SleepLog sleepLog : sleepLogList) {
                SleepLog old = sleepLogMap.get(sleepLog.getDateLog());

                if (old != null) {
                    // Comprobamos si los datos recogidos son iguales.
                    // No uso el equals porque ya está implementado comparando los identificadores.
                    if (!old.equalsByAttributes(sleepLog)) {
                        sleepLogMap.remove(sleepLog.getDateLog());
                        sleepLog.setSleepLogId(old.getSleepLogId());
                        sleepLogMap.put(sleepLog.getDateLog(), sleepLog);
                    }
                } else {
                    sleepLogMap.put(sleepLog.getDateLog(), sleepLog);
                }
            }

            this.sleepLogList = new ArrayList<>(sleepLogMap.values());
        }
    }

    private void daysReceivedFromFitbit() {
        List<String> received = new ArrayList();

        for (ActivityLog activityLog : activityLogList) {
            received.add(Constants.dfus.format(activityLog.getDateLog()));
        }

        daysReceivedFromFitbit = String.join(",", received.toArray(new String[received.size()]));
    }

    private void daysSentToZtreamy() {
        List<String> sent = new ArrayList();

        for (ActivityLog activityLog : activityLogList) {
            if (activityLog.isSent()) {
                sent.add(Constants.dfus.format(activityLog.getDateLog()));
            }
        }

        daysSentToZtreamy = String.join(",", sent.toArray(new String[sent.size()]));
    }

    public String getDaysReceivedFromFitbit() {
        return daysReceivedFromFitbit;
    }

    public String getDaysSentToZtreamy() {
        return daysSentToZtreamy;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Person)) {
            return false;
        }
        Person other = (Person) object;

        // Dos elementos serán iguales si tienen el mismo id.
        return new EqualsBuilder().
                append(this.personId, other.personId).
                isEquals();
    }

    public ActivityLog getActivityLog(Date selectedDate, String aggregation) {
        ActivityLog activityLog = getActivityLog(selectedDate);
        if (activityLog != null) {
            activityLog.setAggregation(aggregation);
        }

        return activityLog;
    }

    public ActivityLog getActivityLog(Date date) {
        if (activityLogMap == null) {
            prepareActivityLogMap();
        }

        return activityLogMap.get(date);
    }

    public List<ActivityLog> getActivityLogList() {
        return activityLogList;
    }

//    public void deleteActivityLogList(Date startDate, Date endDate) {
//        LocalDate startLocalDate;
//        LocalDate endLocalDate;
//
//        if (startDate == null) {
//            startLocalDate = new LocalDate(Long.MIN_VALUE);
//        } else {
//            startLocalDate = new LocalDate(startDate);
//        }
//        if (endDate == null) {
//            endLocalDate = new LocalDate(Long.MAX_VALUE);
//        } else {
//            endLocalDate = new LocalDate(endDate);
//        }
//
//        if (activityLogList != null) {
//            List<ActivityLog> tempActivityLogList = new ArrayList(activityLogList);
//            for (ActivityLog activityLog : tempActivityLogList) {
//                // Comprobamos si la fecha de la actividad está en el rango de fechas que quiere borrar el usuario.
//                LocalDate tempLocalDate = new LocalDate(activityLog.getDateLog());
//                if ((tempLocalDate.isAfter(startLocalDate) && tempLocalDate.isBefore(endLocalDate))
//                        || tempLocalDate.isEqual(startLocalDate)
//                        || tempLocalDate.isEqual(endLocalDate)) {
//                    activityLogList.remove(activityLog);
//                }
//            }
//        }
//    }
    public void setActivityLogList(List<ActivityLog> activityLogList) {
        this.activityLogList = activityLogList;
        prepareActivityLogMap();
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
    public List<ActivityLog> getActivityLogList(Date startDate, Date endDate, String aggregation) {
        List<ActivityLog> filteredList = new ArrayList<>();
        LocalDate startLocalDate;
        LocalDate endLocalDate;

        if (startDate == null) {
            startLocalDate = new LocalDate(Long.MIN_VALUE);
        } else {
            startLocalDate = new LocalDate(startDate);
        }
        if (endDate == null) {
            endLocalDate = new LocalDate(Long.MAX_VALUE);
        } else {
            endLocalDate = new LocalDate(endDate);
        }
        if (activityLogList != null) {
            for (ActivityLog activityLog : activityLogList) {
                // Comprobamos si la fecha de la actividad está en el rango de fechas que solicita el usuario.
                LocalDate tempLocalDate = new LocalDate(activityLog.getDateLog());
                if ((tempLocalDate.isAfter(startLocalDate) && tempLocalDate.isBefore(endLocalDate))
                        || tempLocalDate.isEqual(startLocalDate)
                        || tempLocalDate.isEqual(endLocalDate)) {
                    activityLog.setAggregation(aggregation);
                    filteredList.add(activityLog);
                }
            }
        }

        return filteredList;
    }

    @XmlTransient
    public List<Alert> getAlertList() {
        return alertList;
    }

    public void setAlertList(List<Alert> alertList) {
        this.alertList = alertList;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public boolean getConfigurationBooleanValue(String key) {
        boolean value = false;
        String stringValue = "";

        try {
            stringValue = configurationHashMap.get(key).getValue();
            value = stringValue != null ? Boolean.getBoolean(stringValue) : Boolean.parseBoolean(Constants.getInstance().getConfigurationValueByKey(key));
        } catch (NumberFormatException e) {
            LOG.log(Level.WARNING, "getConfigurationBooleanValue() - El valor [{0}] de la clave [{1}] no es un booleano. Se devolverá un 'false'", new Object[]{stringValue, key});
        }

        return value;
    }

    public HashMap<String, PersonConfiguration> getConfigurationHashMap() {
        return configurationHashMap;
    }

    public int getConfigurationIntValue(String key) {
        int value = 0;
        String stringValue = "";

        try {
            stringValue = configurationHashMap.get(key).getValue();
            value = stringValue != null ? Integer.parseInt(stringValue) : Integer.parseInt(Constants.getInstance().getConfigurationValueByKey(key));
        } catch (NumberFormatException e) {
            LOG.log(Level.WARNING, "getConfigurationIntValue() - El valor [{0}] de la clave [{1}] no es un entero. Se devolverá un '0'", new Object[]{stringValue, key});
        }

        return value;
    }

    public List<PersonConfiguration> getConfigurationList() {
        return configurationList;
    }

    public void setConfigurationList(List<PersonConfiguration> configurationList) {
        this.configurationList = configurationList;
        prepareConfigurationHashMap();
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSha() {
        if (sha == null || sha.length() == 0) {
            sha = new String(Hex.encodeHex(DigestUtils.sha256(email)));
        }

        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public Date getFirstSynchronization() {
        Date firstSyncDate = null;

        // Habrá que tener en cuenta todos los elementos que se pueden sincronizar.
        for (Constants.HermesServices service : Constants.HermesServices.values()) {
            Date serviceFirstSynchronization = getFirstSynchronization(service);

            if (serviceFirstSynchronization != null) {
                if (firstSyncDate == null) {
                    firstSyncDate = serviceFirstSynchronization;
                    // JYFR: En lugar de coger la fecha más tardía de la sincronización de todos los servicios, vamos a coger la más temprana de cualquiera de los servicios.
//                } else if (serviceFirstSynchronization.after(firstSyncDate)) {
                } else if (serviceFirstSynchronization.before(firstSyncDate)) {
                    firstSyncDate = serviceFirstSynchronization;
                }
            }
        }

        return firstSyncDate;
    }

    public Date getFirstFitbitSynchronization() {
        Date firstSyncDate = null;

        // Habrá que tener en cuenta todos los elementos que se pueden sincronizar.
        for (Constants.FitbitServices service : Constants.FitbitServices.values()) {
            Date serviceFirstSynchronization = getFirstFitbitSynchronization(service);

            if (serviceFirstSynchronization != null) {
                if (firstSyncDate == null) {
                    firstSyncDate = serviceFirstSynchronization;
                    // JYFR: En lugar de coger la fecha más tardía de la sincronización de todos los servicios, vamos a coger la más temprana de cualquiera de los servicios.
//                } else if (serviceFirstSynchronization.after(firstSyncDate)) {
                } else if (serviceFirstSynchronization.before(firstSyncDate)) {
                    firstSyncDate = serviceFirstSynchronization;
                }
            }
        }

        return firstSyncDate;
    }

    public String getFitbitAccessToken() {
        return fitbitAccessToken;
    }

    public void setFitbitAccessToken(String fitbitAccessToken) {
        this.fitbitAccessToken = fitbitAccessToken;
    }

    public String getFitbitRefreshToken() {
        return fitbitRefreshToken;
    }

    public void setFitbitRefreshToken(String fitbitRefreshToken) {
        this.fitbitRefreshToken = fitbitRefreshToken;
    }

    public String getFitbitUserId() {
        return fitbitUserId;
    }

    public void setFitbitUserId(String fitbitUserId) {
        this.fitbitUserId = fitbitUserId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public HealthLog getHealthLog(Date selectedDate, String aggregation) {
        HealthLog healthLog = healthLogMap.get(selectedDate);
        if (healthLog != null) {
            healthLog.setAggregation(aggregation);
        }

        return healthLog;
    }

    public HealthLog getHealthLog(Date date) {
        if (healthLogMap == null) {
            prepareHealthLogMap();
        }

        return healthLogMap.get(date);
    }

    @XmlTransient
    public List<HealthLog> getHealthLogList() {
        return healthLogList;
    }

    public void setHealthLogList(List<HealthLog> healthLogList) {
        this.healthLogList = healthLogList;
        prepareHealthLogMap();
    }

    /**
     * Método para obtener los datos de ritmo cardíaco desde una fecha de inicio
     * a una fecha de fin, agregados según la forma indicada. Si la agregación
     * es nula o un valor no válido, los datos se devolverán por minuto.
     *
     * @param startDate Fecha de inicio (incluida)
     * @param endDate Fecha de fin (incluida)
     * @param aggregation Modo de agregación. Si el valor es nulo o un valor no
     * válido, los datos se devolverán por minuto.
     * @return
     */
    public List<HealthLog> getHealthLogList(Date startDate, Date endDate, String aggregation) {
        List<HealthLog> filteredList = new ArrayList<>();
        LocalDate startLocalDate;
        LocalDate endLocalDate;

        if (startDate == null) {
            startLocalDate = new LocalDate(Long.MIN_VALUE);
        } else {
            startLocalDate = new LocalDate(startDate);
        }
        if (endDate == null) {
            endLocalDate = new LocalDate(Long.MAX_VALUE);
        } else {
            endLocalDate = new LocalDate(endDate);
        }
        if (healthLogList != null) {
            for (HealthLog healthLog : healthLogList) {
                // Comprobamos si la fecha de la actividad está en el rango de fechas que solicita el usuario.
                LocalDate tempLocalDate = new LocalDate(healthLog.getDateLog());
                if ((tempLocalDate.isAfter(startLocalDate) && tempLocalDate.isBefore(endLocalDate))
                        || tempLocalDate.isEqual(startLocalDate)
                        || tempLocalDate.isEqual(endLocalDate)) {
                    healthLog.setAggregation(aggregation);
                    filteredList.add(healthLog);
                }
            }
        }

        return filteredList;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Date getLastFitbitSynchronization() {
        Date lastSyncDate = null;

        // Habrá que tener en cuenta todos los elementos que se pueden sincronizar.
        for (Constants.FitbitServices service : Constants.FitbitServices.values()) {
            Date serviceLastSynchronization = getLastFitbitSynchronization(service);

            if (serviceLastSynchronization != null) {
                if (lastSyncDate == null) {
                    lastSyncDate = serviceLastSynchronization;
                    // JYFR: En lugar de coger la fecha más temprana de la sincronización de todos los servicios, vamos a coger la más tardía de cualquiera de los servicios.
//                } else if (serviceLastSynchronization.before(lastSyncDate)) {
                } else if (serviceLastSynchronization.after(lastSyncDate)) {
                    lastSyncDate = serviceLastSynchronization;
                }
            }
        }

        return lastSyncDate;
    }

    public Date getLastSynchronization() {
        Date lastSyncDate = null;

        // Habrá que tener en cuenta todos los elementos que se pueden sincronizar.
        for (Constants.HermesServices service : Constants.HermesServices.values()) {
            Date serviceLastSynchronization = getLastSynchronization(service);

            if (serviceLastSynchronization != null) {
                if (lastSyncDate == null) {
                    lastSyncDate = serviceLastSynchronization;
                    // JYFR: En lugar de coger la fecha más temprana de la sincronización de todos los servicios, vamos a coger la más tardía de cualquiera de los servicios.
//                } else if (serviceLastSynchronization.before(lastSyncDate)) {
                } else if (serviceLastSynchronization.after(lastSyncDate)) {
                    lastSyncDate = serviceLastSynchronization;
                }
            }
        }

        return lastSyncDate;
    }

    public List<LocationLog> getLocationLogList() {
        return locationLogList;
    }

    public void setLocationLogList(List<LocationLog> locationLogList) {
        this.locationLogList = locationLogList;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getPersonId() {
        return personId;
    }

    public void setPersonId(Integer personId) {
        this.personId = personId;
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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public SleepLog getSleepLog(Date selectedDate) {
        if (sleepLogMap == null) {
            prepareSleepLogMap();
        }

        return sleepLogMap.get(selectedDate);
    }

    public List<SleepLog> getSleepLogList() {
        return sleepLogList;
    }

//    public void deleteSleepLogList(Date startDate, Date endDate) {    
//        LocalDate startLocalDate;
//        LocalDate endLocalDate;
//
//        if (startDate == null) {
//            startLocalDate = new LocalDate(Long.MIN_VALUE);
//        } else {
//            startLocalDate = new LocalDate(startDate);
//        }
//        if (endDate == null) {
//            endLocalDate = new LocalDate(Long.MAX_VALUE);
//        } else {
//            endLocalDate = new LocalDate(endDate);
//        }
//
//        if (sleepLogList != null) {
//            List<SleepLog> tempSleepLogList = new ArrayList(sleepLogList);
//            for (SleepLog sleepLog : tempSleepLogList) {
//                // Comprobamos si la fecha del registro de sueño está en el rango de fechas que quiere borrar el usuario.
//                LocalDate tempLocalDate = new LocalDate(sleepLog.getDateLog());
//                if ((tempLocalDate.isAfter(startLocalDate) && tempLocalDate.isBefore(endLocalDate))
//                        || tempLocalDate.isEqual(startLocalDate)
//                        || tempLocalDate.isEqual(endLocalDate)) {
//
//                    sleepLogList.remove(sleepLog);
//                }
//            }
//        }
//    }
//    public void deleteHealthLogList(Date startDate, Date endDate) {
//        LocalDate startLocalDate;
//        LocalDate endLocalDate;
//
//        if (startDate == null) {
//            startLocalDate = new LocalDate(Long.MIN_VALUE);
//        } else {
//            startLocalDate = new LocalDate(startDate);
//        }
//        if (endDate == null) {
//            endLocalDate = new LocalDate(Long.MAX_VALUE);
//        } else {
//            endLocalDate = new LocalDate(endDate);
//        }
//
//        if (healthLogList != null) {
//            List<HealthLog> tempHealthLogList = new ArrayList(healthLogList);
//            for (HealthLog healthLog : tempHealthLogList) {
//                // Comprobamos si la fecha del registro de salud está en el rango de fechas que quiere borrar el usuario.
//                LocalDate tempLocalDate = new LocalDate(healthLog.getDateLog());
//                if ((tempLocalDate.isAfter(startLocalDate) && tempLocalDate.isBefore(endLocalDate))
//                        || tempLocalDate.isEqual(startLocalDate)
//                        || tempLocalDate.isEqual(endLocalDate)) {
//
//                    healthLogList.remove(healthLog);
//                }
//            }
//        }
//    }
    public void setSleepLogList(List<SleepLog> sleepLogList) {
        this.sleepLogList = sleepLogList;
        prepareSleepLogMap();
    }

    /**
     * Método para obtener los datos de sueño desde una fecha de inicio a una
     * fecha de fin.
     *
     * @param startDate Fecha de inicio (incluida)
     * @param endDate Fecha de fin (incluida)
     * @return
     */
    public List<SleepLog> getSleepLogList(Date startDate, Date endDate) {
        List<SleepLog> filteredList = new ArrayList<>();
        LocalDate startLocalDate;
        LocalDate endLocalDate;

        if (startDate == null) {
            startLocalDate = new LocalDate(Long.MIN_VALUE);
        } else {
            startLocalDate = new LocalDate(startDate);
        }
        if (endDate == null) {
            endLocalDate = new LocalDate(Long.MAX_VALUE);
        } else {
            endLocalDate = new LocalDate(endDate);
        }

        if (sleepLogList != null) {
            for (SleepLog sleepLog : sleepLogList) {
                // Comprobamos si la fecha del sueño está en el rango de fechas que solicita el usuario.
                LocalDate tempLocalDate = new LocalDate(sleepLog.getDateLog());
                if ((tempLocalDate.isAfter(startLocalDate) && tempLocalDate.isBefore(endLocalDate))
                        || tempLocalDate.isEqual(startLocalDate)
                        || tempLocalDate.isEqual(endLocalDate)) {
                    filteredList.add(sleepLog);
                }
            }
        }

        return filteredList;
    }

    public List<SleepLog> getSleepLogPendingToSendToZtreamy() {
        List sleepLogPendingToSendToZtreamy = new ArrayList();

        for (SleepLog sleepLog : sleepLogList) {
            if (!sleepLog.isSent()) {
                sleepLogPendingToSendToZtreamy.add(sleepLog);
            }
        }

        return sleepLogPendingToSendToZtreamy;
    }

    public int getThisWeekSessions() {
        return thisWeekSessions;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean hasFitbitCredentials() {
        return (fitbitUserId != null
                && fitbitUserId.length() > 0
                && fitbitAccessToken != null
                && fitbitAccessToken.length() > 0
                && fitbitRefreshToken != null
                && fitbitRefreshToken.length() > 0);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 29).
                append(username).
                toHashCode();
    }

    public boolean isAdmin() {
        return this.role.getRoleId().equals(Constants.ADMINISTRATOR_ROLE);
    }

    public boolean isAlertIfUnableToSynchronize() {
        PersonConfiguration pc = configurationHashMap.get(Person.PersonOptions.AlertIfUnableToSynchronize.name());
        alertIfUnableToSynchronize = pc != null ? Boolean.parseBoolean(pc.getValue()) : false;
        return alertIfUnableToSynchronize;
    }

    public void setAlertIfUnableToSynchronize(boolean alertIfUnableToSynchronize) {
        this.alertIfUnableToSynchronize = alertIfUnableToSynchronize;
        PersonConfiguration pc = configurationHashMap.get(Person.PersonOptions.AlertIfUnableToSynchronize.name());
        pc.setValue(Boolean.toString(alertIfUnableToSynchronize));
    }

    public Date getAlertNotificationsTime() {
        PersonConfiguration pc = configurationHashMap.get(Person.PersonOptions.AlertsNotificationTime.name());

        try {
            alertNotificationsTime = Constants.dfSimpleTime.parse(pc.getValue());
        } catch (Exception ex) {
            alertNotificationsTime = null;
        }

        return alertNotificationsTime;
    }

    public void setAlertNotificationsTime(Date alertNotificationsTime) {
        PersonConfiguration pc = configurationHashMap.get(Person.PersonOptions.AlertsNotificationTime.name());
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTime(alertNotificationsTime);
        cal.set(Calendar.MINUTE, 0);
        this.alertNotificationsTime = cal.getTime();
        pc.setValue(Constants.dfSimpleTime.format(cal.getTime()));
    }

    public boolean isDoctor() {
        return this.role.getRoleId().equals(Constants.DOCTOR_ROLE);
    }

    private void prepareConfiguration() {
        // Analizamos las configuraciones que tiene asignadas la persona, por si faltan.
        if (configurationList == null || configurationList.size() < PersonOptions.values().length) {
            LOG.log(Level.INFO, "prepareConfiguration() - Analizando las configuraciones de la persona: {0}", getFullName());
            for (Person.PersonOptions option : Person.PersonOptions.values()) {
                boolean found = false;
                for (PersonConfiguration pc : configurationList) {
                    if (option.name().equals(pc.getOption().getOptionKey())) {
                        found = true;
                        break;
                    }
                }

                // Si es una opción que no tiene la persona, la añadimos, con el valor por defecto.
                if (!found) {
                    LOG.log(Level.WARNING, "prepareConfiguration() - No se ha encontrado la configuración: {0}. Se creará y se asignará el valor por defecto global del sistema", option.name());
                    try {
                        PersonConfiguration pc = new PersonConfiguration();
                        pc.setOption(Constants.getInstance().getConfigurationByKey(option.name()));
                        pc.setPerson(this);
                        pc.setValue(pc.getOption().getOptionValue());
                        this.getConfigurationList().add(pc);
                    } catch (NullPointerException ex) {
                        LOG.log(Level.SEVERE, "prepareConfiguration() - No se ha encontrado la configuración global {0}. Debe ser definida en la configuración por el administrador.", option.name());
                    }
                }
            }
        }

        prepareConfigurationHashMap();

        // Cargamos el tema visual del usuario.
        PersonConfiguration pc = configurationHashMap.get(Person.PersonOptions.Theme.name());
        theme = new ThemeBean(pc.getValue());
    }

    private void prepareConfigurationHashMap() {

        this.configurationHashMap = new HashMap<>();

        if (configurationList != null) {
            for (PersonConfiguration pc : configurationList) {
                if (pc.getOption() != null) {
                    configurationHashMap.put(pc.getOption().getOptionKey(), pc);
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        sb.append(this.getFullName());
        sb.append("]");

        return sb.toString();
    }

    public void uploadPhoto(FileUploadEvent event) {
        UploadedFile file = event.getFile();

        try {
            photo = IOUtils.toByteArray(file.getInputstream());
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "uploadPhoto() - Error al subir la foto", ex);
        }
    }

    private void addConfiguration(Configuration c) {
        PersonConfiguration pc;
        if (c != null) {
            pc = new PersonConfiguration();
            pc.setPerson(this);
            pc.setOption(c);
            pc.setValue(c.getOptionValue());
            configurationList.add(pc);
        }
    }

//    private void calculateSessions() {
//        thisWeekSessions = 0;
//
//        Calendar cal = Calendar.getInstance();
//        int currentWeek = cal.get(Calendar.WEEK_OF_YEAR);
//
//        for (ActivityLog al : activityLogList) {
//            cal.setTime(al.getDateLog());
//            int week = cal.get(Calendar.WEEK_OF_YEAR);
//            if (week == currentWeek) {
//                // Vamos sumando las sesiones de cada día de la semana.
//                thisWeekSessions += al.getSessionsTotal();
//            } else {
//                // Como la colección de actividades viene ordenada por fecha descendentemente,
//                // cuando sea otra semana podemos salir del bucle.
//                break;
//            }
//        }
//    }
    private Date getFirstSynchronization(Constants.HermesServices service) {
        switch (service) {
            case Steps:
                if (activityLogList != null && !activityLogList.isEmpty()) {
                    // Al tener la colección de actividades una ordenación ascendente, la actividad más antigua será la que esté en primera posición.
                    return activityLogList.get(0).getDateLog();
                } else {
                    return null;
                }
            case Sleep:
                if (sleepLogList != null && !sleepLogList.isEmpty()) {
                    // Al tener la colección de registros de sueño una ordenación ascendente, el registro de sueño más antiguo será la que esté en primera posición.
                    return sleepLogList.get(0).getDateLog();
                } else {
                    return null;
                }
            case HeartRate:
                if (healthLogList != null && !healthLogList.isEmpty()) {
                    // Al tener la colección de registros de salud una ordenación ascendente, el registro de salud más antiguo será la que esté en primera posición.
                    return healthLogList.get(0).getDateLog();
                } else {
                    return null;
                }
            case Location:
                if (locationLogList != null && !locationLogList.isEmpty()) {
                    // Al tener la colección de registros de localización una ordenación ascendente, el registro de localización más antiguo será la que esté en primera posición.
                    return locationLogList.get(0).getDateLog();
                } else {
                    return null;
                }
            case Context:
                if (contextLogList != null && !contextLogList.isEmpty()) {
                    // Al tener la colección de registros de contexto una ordenación ascendente, el registro de contexto más antiguo será la que esté en primera posición.
                    return contextLogList.get(0).getDateLog();
                } else {
                    return null;
                }
            default:
                return null;
        }
    }

    private Date getFirstFitbitSynchronization(Constants.FitbitServices service) {
        switch (service) {
            case Steps:
                if (activityLogList != null && !activityLogList.isEmpty()) {
                    // Al tener la colección de actividades una ordenación ascendente, la actividad más antigua será la que esté en primera posición.
                    return activityLogList.get(0).getDateLog();
                } else {
                    return null;
                }
            case Sleep:
                if (sleepLogList != null && !sleepLogList.isEmpty()) {
                    // Al tener la colección de registros de sueño una ordenación ascendente, el registro de sueño más antiguo será la que esté en primera posición.
                    return sleepLogList.get(0).getDateLog();
                } else {
                    return null;
                }
            case HeartRate:
                if (healthLogList != null && !healthLogList.isEmpty()) {
                    // Al tener la colección de registros de salud una ordenación ascendente, el registro de salud más antiguo será la que esté en primera posición.
                    return healthLogList.get(0).getDateLog();
                } else {
                    return null;
                }
            default:
                return null;
        }
    }

    /**
     * Método para analizar la última sincronización con el origen de datos de
     * los distintos servicios que pueden suministrar información a Hermes.
     *
     * @param service Servicio de datos del que analizar la última
     * sincronización.
     * @return Fecha del servicio actualizado más recientemente.
     */
    private Date getLastSynchronization(Constants.HermesServices service) {
        switch (service) {
            case Steps:
                if (activityLogList != null && !activityLogList.isEmpty()) {
                    // Al tener la colección de actividades una ordenación ascendente, la actividad más reciente será la que esté en última posición.
                    return activityLogList.get(activityLogList.size() - 1).getDateLog();
                } else {
                    return null;
                }
            case Sleep:
                if (sleepLogList != null && !sleepLogList.isEmpty()) {
                    // Al tener la colección de registros de sueño una ordenación ascendente, el registro de sueño más reciente será la que esté en última posición.
                    return sleepLogList.get(sleepLogList.size() - 1).getDateLog();
                } else {
                    return null;
                }
            case HeartRate:
                if (healthLogList != null && !healthLogList.isEmpty()) {
                    // Al tener la colección de registros de salud una ordenación ascendente, el registro de salud más reciente será la que esté en última posición.
                    return healthLogList.get(healthLogList.size() - 1).getDateLog();
                } else {
                    return null;
                }
            case Location:
                if (locationLogList != null && !locationLogList.isEmpty()) {
                    // Al tener la colección de registros de localización una ordenación ascendente, el registro de localización más reciente será la que esté en última posición.
                    return locationLogList.get(locationLogList.size() - 1).getDateLog();
                } else {
                    return null;
                }
            case Context:
                if (contextLogList != null && !contextLogList.isEmpty()) {
                    // Al tener la colección de registros de contexto una ordenación ascendente, el registro de contexto más reciente será la que esté en última posición.
                    return contextLogList.get(contextLogList.size() - 1).getDateLog();
                } else {
                    return null;
                }
            default:
                return null;
        }
    }

    private Date getLastFitbitSynchronization(Constants.FitbitServices service) {
        switch (service) {
            case Steps:
                if (activityLogList != null && !activityLogList.isEmpty()) {
                    // Al tener la colección de actividades una ordenación ascendente, la actividad más reciente será la que esté en última posición.
                    return activityLogList.get(activityLogList.size() - 1).getDateLog();
                } else {
                    return null;
                }
            case Sleep:
                if (sleepLogList != null && !sleepLogList.isEmpty()) {
                    // Al tener la colección de registros de sueño una ordenación ascendente, el registro de sueño más reciente será la que esté en última posición.
                    return sleepLogList.get(sleepLogList.size() - 1).getDateLog();
                } else {
                    return null;
                }
            case HeartRate:
                if (healthLogList != null && !healthLogList.isEmpty()) {
                    // Al tener la colección de registros de salud una ordenación ascendente, el registro de salud más reciente será la que esté en última posición.
                    return healthLogList.get(healthLogList.size() - 1).getDateLog();
                } else {
                    return null;
                }
            default:
                return null;
        }
    }

    // JYFR: Método que será invocado automáticamente tras cargar los datos de la base de datos y de ser inyectados en los atributos correspondientes.
    @PostLoad
    private void init() {
        // TODO: Ver si pasa al crear una persona nueva.
        prepareConfiguration();
//        calculateSessions();
    }

    private void prepareActivityLogMap() {
        this.activityLogMap = new TreeMap<>();

        if (activityLogList != null) {
            for (ActivityLog a : activityLogList) {
                if (a.getDateLog() != null) {
                    activityLogMap.put(a.getDateLog(), a);
                }
            }
        }
        daysReceivedFromFitbit();
        daysSentToZtreamy();
    }

    private void prepareHealthLogMap() {
        this.healthLogMap = new TreeMap<>();

        if (healthLogList != null) {
            for (HealthLog h : healthLogList) {
                if (h.getDateLog() != null) {
                    healthLogMap.put(h.getDateLog(), h);
                }
            }
        }
    }

    private void prepareSleepLogMap() {
        this.sleepLogMap = new TreeMap<>();

        if (sleepLogList != null) {
            for (SleepLog s : sleepLogList) {
                if (s.getDateLog() != null) {
                    sleepLogMap.put(s.getDateLog(), s);
                }
            }
        }
    }

    private void prepareContextLogMap() {
        this.contextLogMap = new TreeMap<>();

        if (contextLogList != null) {
            for (ContextLog c : contextLogList) {
                if (c.getDateLog() != null) {
                    contextLogMap.put(c.getDateLog(), c);
                }
            }
        }
    }

    public static enum PersonOptions {

        StepsGoal, MinimumSessionMinutes, MaximumSessionMinutes, EndSessionStoppedMinutes, RestStepsThreshold, RestMinutesThreshold, SessionsPerWeek, AlertIfUnableToSynchronize, AlertsNotificationTime, Theme
    }

    @XmlTransient
    public List<ContextLog> getContextLogList() {
        return contextLogList;
    }

    public void setContextLogList(List<ContextLog> contextLogList) {
        this.contextLogList = contextLogList;
    }

    public ContextLog getContextLog(Date date) {
        if (contextLogMap == null) {
            prepareContextLogMap();
        }

        return contextLogMap.get(date);
    }

    public ThemeBean getTheme() {
        return theme;
    }

    public void setTheme(ThemeBean theme) {
        this.theme = theme;
    }

    /**
     * Método para obtener los datos de contexto desde una fecha de inicio a una
     * fecha de fin, agregados según la forma indicada. Si la agregación es nula
     * o un valor no válido, los datos se devolverán por minuto.
     *
     * @param startDate Fecha de inicio (incluida)
     * @param endDate Fecha de fin (incluida)
     * @param aggregation Modo de agregación. Si el valor es nulo o un valor no
     * válido, los datos se devolverán por minuto.
     * @return
     */
    public List<ContextLog> getContextLogList(Date startDate, Date endDate, String aggregation) {
        List<ContextLog> filteredList = new ArrayList<>();
        LocalDate startLocalDate;
        LocalDate endLocalDate;

        if (startDate == null) {
            startLocalDate = new LocalDate(Long.MIN_VALUE);
        } else {
            startLocalDate = new LocalDate(startDate);
        }
        if (endDate == null) {
            endLocalDate = new LocalDate(Long.MAX_VALUE);
        } else {
            endLocalDate = new LocalDate(endDate);
        }
        if (contextLogList != null) {
            for (ContextLog contextLog : contextLogList) {
                // Comprobamos si la fecha del contecto está en el rango de fechas que solicita el usuario.
                LocalDate tempLocalDate = new LocalDate(contextLog.getDateLog());
                if ((tempLocalDate.isAfter(startLocalDate) && tempLocalDate.isBefore(endLocalDate))
                        || tempLocalDate.isEqual(startLocalDate)
                        || tempLocalDate.isEqual(endLocalDate)) {
                    // TODO: Agregaciones
//                    contextLog.setAggregation(aggregation);
                    filteredList.add(contextLog);
                }
            }
        }

        return filteredList;
    }
}
