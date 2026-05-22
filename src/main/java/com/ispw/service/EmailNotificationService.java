package com.ispw.service;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.service.interfaces.EmailNotification;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * Implementazione concreta del servizio di notifica email tramite SMTP (Gmail).
 * Questa classe usa Jakarta Mail per inviare realmente le email.
 */
public class EmailNotificationService implements EmailNotification {

    private static final Logger LOGGER =
        Logger.getLogger(EmailNotificationService.class.getName());

    // Email mittente (account Gmail)
    private final String from;

    // Password applicativa (NON la password reale)
    private final String password;

    /**
     * Costruttore con parametri per evitare hardcode e migliorare riusabilità.
     */
    public EmailNotificationService(String from, String password) {
        this.from = from;
        this.password = password;
    }

    /**
     * Invia una email utilizzando protocollo SMTP.
     */
    @Override
    public void sendNotification(String to, String subject, String messageText) {

        // Configurazione proprietà SMTP per Gmail
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Creazione sessione autenticata
        Session session = Session.getInstance(props,
            new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(from, password);
                }
            }
        );

        try {
            // Creazione messaggio email
            Message msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress(from));

            msg.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(to)
            );

            msg.setSubject(subject);
            msg.setText(messageText);

            // Invio effettivo dell'email
            Transport.send(msg);

            LOGGER.log(Level.INFO, "Email inviata a: {0}", to);
        } catch (MessagingException e) {
            // Logging errore senza bloccare il flusso principale
            LOGGER.log(Level.SEVERE, "Errore invio email", e);
        }
    }
}