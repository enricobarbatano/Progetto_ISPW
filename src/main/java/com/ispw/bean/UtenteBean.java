package com.ispw.bean;


import com.ispw.model.enums.Ruolo;

public class UtenteBean {
    private String nome;
    private String cognome;
    private String email;
    private Ruolo ruolo;

    public UtenteBean() {}

    public UtenteBean(String nome, String cognome, String email, Ruolo ruolo) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.ruolo = ruolo;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Ruolo getRuolo() { return ruolo; }
    public void setRuolo(Ruolo ruolo) { this.ruolo = ruolo; }
}
