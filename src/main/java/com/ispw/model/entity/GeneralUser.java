package com.ispw.model.entity;

import com.ispw.model.enums.Ruolo;
import com.ispw.model.enums.StatoAccount;

public abstract class GeneralUser {
    private int idUtente;
    private String nome;
    private String cognome;
    private String email;
    private String password;
    private StatoAccount statoAccount;
    private Ruolo ruolo;

    protected GeneralUser(int idUtente, String nome, String email, String password, StatoAccount statoAccount, Ruolo ruolo) {
        this.idUtente = idUtente;
        this.nome = nome;
        this.email = email;
        this.password = password;
        this.statoAccount = statoAccount;
        this.ruolo = ruolo;
    }

    public int getIdUtente() { return idUtente; }
    public String getNome() { return nome; }
    public String getCognome() { return cognome; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public StatoAccount getStatoAccount() { return statoAccount; }
    public Ruolo getRuolo() { return ruolo; }
}

