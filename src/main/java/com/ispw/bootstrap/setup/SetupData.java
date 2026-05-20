package com.ispw.bootstrap.setup;

import java.util.List;

import com.ispw.model.entity.Campo;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.entity.Gestore;
import com.ispw.model.entity.RegolePenalita;
import com.ispw.model.entity.RegoleTempistiche;
import com.ispw.model.entity.UtenteFinale;

/**
 * DTO dei dati di setup iniziale.
 *
 * Responsabilità:
 * - rappresentare il contenuto del file setup.json;
 * - fornire una struttura semplice a Jackson per la deserializzazione;
 * - trasportare i dati iniziali verso la fase di bootstrap.
 *
 * Nota:
 * i campi sono privati per rispettare l'incapsulamento.
 * Jackson può comunque valorizzarli tramite costruttore vuoto e setter.
 */
public class SetupData {

    /**
     * Lista degli utenti generici presenti nel file di setup.
     */
    private List<GeneralUser> generalUsers;

    /**
     * Lista degli utenti finali presenti nel file di setup.
     */
    private List<UtenteFinale> utentiFinali;

    /**
     * Lista dei gestori presenti nel file di setup.
     */
    private List<Gestore> gestori;

    /**
     * Lista dei campi sportivi presenti nel file di setup.
     */
    private List<Campo> campi;

    /**
     * Configurazione iniziale delle regole di penalità.
     */
    private RegolePenalita regolePenalita;

    /**
     * Configurazione iniziale delle regole temporali.
     */
    private RegoleTempistiche regoleTempistiche;

    /**
     * Costruttore vuoto richiesto da Jackson.
     *
     * Jackson crea l'oggetto e poi valorizza i campi usando i setter.
     */
    public SetupData() {
        // DTO per deserializzazione JSON: costruttore vuoto intenzionale.
    }

    public List<GeneralUser> getGeneralUsers() {
        return generalUsers;
    }

    public void setGeneralUsers(List<GeneralUser> generalUsers) {
        this.generalUsers = generalUsers;
    }

    public List<UtenteFinale> getUtentiFinali() {
        return utentiFinali;
    }

    public void setUtentiFinali(List<UtenteFinale> utentiFinali) {
        this.utentiFinali = utentiFinali;
    }

    public List<Gestore> getGestori() {
        return gestori;
    }

    public void setGestori(List<Gestore> gestori) {
        this.gestori = gestori;
    }

    public List<Campo> getCampi() {
        return campi;
    }

    public void setCampi(List<Campo> campi) {
        this.campi = campi;
    }

    public RegolePenalita getRegolePenalita() {
        return regolePenalita;
    }

    public void setRegolePenalita(RegolePenalita regolePenalita) {
        this.regolePenalita = regolePenalita;
    }

    public RegoleTempistiche getRegoleTempistiche() {
        return regoleTempistiche;
    }

    public void setRegoleTempistiche(RegoleTempistiche regoleTempistiche) {
        this.regoleTempistiche = regoleTempistiche;
    }
}