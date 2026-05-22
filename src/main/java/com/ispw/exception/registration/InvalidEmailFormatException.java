package com.ispw.exception.registration;


public class InvalidEmailFormatException extends RegistrationException {

    public InvalidEmailFormatException() {
        super("Formato email non valido. Deve essere del tipo nome@dominio.estensione");
    }
}
