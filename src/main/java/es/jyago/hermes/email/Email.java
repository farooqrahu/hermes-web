/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.jyago.hermes.email;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author Jorge Yago
 */
@Singleton
@Startup
public class Email {

    private static final Logger log = Logger.getLogger(Email.class.getName());

    private static Properties mailServerProperties;
    private static Session mailSession;

    @PostConstruct
    public void onStartup() {
        log.log(Level.INFO, "onStartup() - Inicialización del gestor de correo");

        mailServerProperties = System.getProperties();
        mailServerProperties.put("mail.smtp.port", "587");
        mailServerProperties.put("mail.smtp.auth", "true");
        mailServerProperties.put("mail.smtp.starttls.enable", "true");
        mailSession = Session.getDefaultInstance(mailServerProperties, null);
    }

    public static void generateAndSendEmail(String recipient, String subject, String body) throws AddressException, MessagingException {
        log.log(Level.INFO, "generateAndSendEmail() - Generación y envío del correo a: {0}", recipient);

        MimeMessage generateMailMessage = new MimeMessage(mailSession);
        generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

        // Asunto.
        generateMailMessage.setSubject(subject);
        // Cuerpo.
        generateMailMessage.setContent(body, "text/html");
        log.log(Level.INFO, "generateAndSendEmail() - Email generado correctamente");

        Transport transport = mailSession.getTransport("smtp");
        transport.connect("smtp.gmail.com", "hermes.web.citizen@gmail.com", "hermes2015");
        transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
        transport.close();
        log.log(Level.INFO, "generateAndSendEmail() - Email enviado correctamente");
    }

    public static void generateAndSendEmailToAdministrator(String subject, String body) throws AddressException, MessagingException {
        log.log(Level.INFO, "generateAndSendEmailToAdministrator() - Envío de mensaje al administrador");
        generateAndSendEmail("hermes.web.citizen@gmail.com", subject, body);
    }
}
