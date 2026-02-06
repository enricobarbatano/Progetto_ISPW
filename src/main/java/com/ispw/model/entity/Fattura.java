package com.ispw.model.entity;

import java.io.Serializable;
import java.time.LocalDate;

public final class Fattura implements Serializable {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: entity fattura.
    // A2) IO: riferimenti prenotazione/utente e dati fiscali.
    // ========================
    private int idFattura;
    private int idPrenotazione;      // FK verso prenotazione
    private int idUtente;            // <-- NUOVO: FK verso utente
    private String codiceFiscaleCliente;
    private LocalDate dataEmissione;
    private String linkPdf;

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) getters/setters: accesso ai campi.
    // ========================

    public int getIdFattura() { return idFattura; }
    public void setIdFattura(int idFattura) { this.idFattura = idFattura; }

    public int getIdPrenotazione() { return idPrenotazione; }
    public void setIdPrenotazione(int idPrenotazione) { this.idPrenotazione = idPrenotazione; }

    public int getIdUtente() { return idUtente; }
    public void setIdUtente(int idUtente) { this.idUtente = idUtente; }

    public String getCodiceFiscaleCliente() { return codiceFiscaleCliente; }
    public void setCodiceFiscaleCliente(String codiceFiscaleCliente) { this.codiceFiscaleCliente = codiceFiscaleCliente; }

    public LocalDate getDataEmissione() { return dataEmissione; }
    public void setDataEmissione(LocalDate dataEmissione) { this.dataEmissione = dataEmissione; }

    public String getLinkPdf() { return linkPdf; }
    public void setLinkPdf(String linkPdf) { this.linkPdf = linkPdf; }
}