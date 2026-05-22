package com.ispw.exception.registration;

public class EmailAlreadyExistsException extends RegistrationException {

    public EmailAlreadyExistsException() {
        super("Email già in uso");
    }
}