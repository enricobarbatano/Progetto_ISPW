package com.ispw.model.entity;

import java.io.Serializable;

import com.ispw.model.enums.Ruolo;
import com.ispw.model.enums.StatoAccount;

public abstract class GeneralUser implements Serializable {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: base entity per utenti.
    // A2) IO: stato anagrafico e credenziali.
    // ========================
    private int idUtente;
    private String nome;
    private String cognome;
    private String email;
    private String password;
    private StatoAccount statoAccount;
    private Ruolo ruolo;

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) getters/setters: accesso ai campi base.
    // ========================

    protected GeneralUser() {}

    protected GeneralUser(int idUtente, String nome,String cognome, String email, String password,StatoAccount statoAccount, Ruolo ruolo) {
        this.idUtente = idUtente;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.password = password;
        this.statoAccount = statoAccount;
        this.ruolo = ruolo;
    }
    public int getIdUtente() { return idUtente; }
    public void setIdUtente(int idUtente) { this.idUtente = idUtente; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public StatoAccount getStatoAccount() { return statoAccount; }
    public void setStatoAccount(StatoAccount statoAccount) { this.statoAccount = statoAccount; }

    public Ruolo getRuolo() { return ruolo; }
    public void setRuolo(Ruolo ruolo) { this.ruolo = ruolo; }
}
