package com.ispw.model.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ispw.model.enums.StatoRichiestaDisdetta;

public final class RichiestaDisdettaRimborso implements Serializable {

    private int idRichiesta;
    private int idPrenotazione;
    private int idUtente;

    private LocalDateTime timestampRichiesta;
    private LocalDateTime timestampDecisione;

    private BigDecimal penaleStimata;
    private BigDecimal rimborsoStimato;

    private StatoRichiestaDisdetta stato;

    private String notaUtente;
    private String notaGestore;

    // opzionale: traccia gestore che decide (se vuoi)
    private Integer idGestoreDecisione;

    public int getIdRichiesta() { return idRichiesta; }
    public void setIdRichiesta(int idRichiesta) { this.idRichiesta = idRichiesta; }

    public int getIdPrenotazione() { return idPrenotazione; }
    public void setIdPrenotazione(int idPrenotazione) { this.idPrenotazione = idPrenotazione; }

    public int getIdUtente() { return idUtente; }
    public void setIdUtente(int idUtente) { this.idUtente = idUtente; }

    public LocalDateTime getTimestampRichiesta() { return timestampRichiesta; }
    public void setTimestampRichiesta(LocalDateTime timestampRichiesta) { this.timestampRichiesta = timestampRichiesta; }

    public LocalDateTime getTimestampDecisione() { return timestampDecisione; }
    public void setTimestampDecisione(LocalDateTime timestampDecisione) { this.timestampDecisione = timestampDecisione; }

    public BigDecimal getPenaleStimata() { return penaleStimata; }
    public void setPenaleStimata(BigDecimal penaleStimata) { this.penaleStimata = penaleStimata; }

    public BigDecimal getRimborsoStimato() { return rimborsoStimato; }
    public void setRimborsoStimato(BigDecimal rimborsoStimato) { this.rimborsoStimato = rimborsoStimato; }

    public StatoRichiestaDisdetta getStato() { return stato; }
    public void setStato(StatoRichiestaDisdetta stato) { this.stato = stato; }

    public String getNotaUtente() { return notaUtente; }
    public void setNotaUtente(String notaUtente) { this.notaUtente = notaUtente; }

    public String getNotaGestore() { return notaGestore; }
    public void setNotaGestore(String notaGestore) { this.notaGestore = notaGestore; }

    public Integer getIdGestoreDecisione() { return idGestoreDecisione; }
    public void setIdGestoreDecisione(Integer idGestoreDecisione) { this.idGestoreDecisione = idGestoreDecisione; }
}
