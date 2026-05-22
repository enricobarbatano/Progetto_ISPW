package com.ispw.exception.registration;

public class InvalidRegistrationDataException extends RegistrationException {

    public InvalidRegistrationDataException() {
        super("Dati registrazione non validi");
    }
}