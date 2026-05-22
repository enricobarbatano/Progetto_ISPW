package com.ispw.service.interfaces;


/**
 * Interfaccia che rappresenta un servizio esterno di invio notifiche via email.
 * Serve per disaccoppiare la logica applicativa dall'implementazione concreta.
 */
public interface EmailNotification {

    /**
     * Invia una notifica email.
     *
     * @param to destinatario (email)
     * @param subject oggetto email
     * @param messageText corpo del messaggio
     */
    void sendNotification(String to, String subject, String messageText);
}
