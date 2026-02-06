package com.ispw.bean;


import com.ispw.model.enums.Ruolo;

public class UtenteBean extends BaseAnagraficaBean {

    private Ruolo ruolo;

    public UtenteBean() {
    }

    public UtenteBean(String nome, String cognome, String email, Ruolo ruolo) {
        setNome(nome);
        setCognome(cognome);
        setEmail(email);
        this.ruolo = ruolo;
    }

    public Ruolo getRuolo() {
        return ruolo;
    }

    public void setRuolo(Ruolo ruolo) {
        this.ruolo = ruolo;
    }
}
