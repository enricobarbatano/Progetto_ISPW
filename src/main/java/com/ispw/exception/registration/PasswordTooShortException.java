package com.ispw.exception.registration;


public class PasswordTooShortException extends RegistrationException {

    public PasswordTooShortException() {
        super("Password deve avere almeno 6 caratteri");
    }
}

