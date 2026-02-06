package com.ispw.bean;

import com.ispw.model.enums.Ruolo;

public class UtenteSelezioneBean {

    private int idUtente;
    private String email;
    private Ruolo ruolo;

    public UtenteSelezioneBean() {
        // costruttore di default
    }

    public int getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(int idUtente) {
        this.idUtente = idUtente;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Ruolo getRuolo() {
        return ruolo;
    }

    public void setRuolo(Ruolo ruolo) {
        this.ruolo = ruolo;
    }
}
