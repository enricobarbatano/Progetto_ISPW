package com.ispw.service;
import com.ispw.service.interfaces.EmailNotification;

/**
 * Factory per la creazione dei servizi esterni.
 */public final class ExternalServiceFactory {

    private static final String EMAIL = "enrybarba2002@gmail.com";
    @SuppressWarnings("java:S2068")
    private static final String APP_PASSWORD = "lxci mjtu jhlt zokg";

    private ExternalServiceFactory() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static EmailNotification createEmailService() {
        return new EmailNotificationService(EMAIL, APP_PASSWORD);
    }
}