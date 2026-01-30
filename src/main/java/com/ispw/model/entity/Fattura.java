package com.ispw.model.entity;

import java.io.Serializable;
import java.time.LocalDate;

public final class Fattura implements Serializable {
    private int idFattura;
    private int idPrenotazione; // FK
    private String codiceFiscaleCliente;
    private LocalDate dataEmissione;
    private String linkPdf;

    public int getIdFattura() { return idFattura; }
    public void setIdFattura(int idFattura) { this.idFattura = idFattura; }

    public int getIdPrenotazione() { return idPrenotazione; }
    public void setIdPrenotazione(int idPrenotazione) { this.idPrenotazione = idPrenotazione; }

    public String getCodiceFiscaleCliente() { return codiceFiscaleCliente; }
    public void setCodiceFiscaleCliente(String codiceFiscaleCliente) { this.codiceFiscaleCliente = codiceFiscaleCliente; }

    public LocalDate getDataEmissione() { return dataEmissione; }
    public void setDataEmissione(LocalDate dataEmissione) { this.dataEmissione = dataEmissione; }

    public String getLinkPdf() { return linkPdf; }
    public void setLinkPdf(String linkPdf) { this.linkPdf = linkPdf; }
}
