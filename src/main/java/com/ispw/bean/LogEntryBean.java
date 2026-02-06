package com.ispw.bean;

import java.time.LocalDateTime;

public class LogEntryBean {

    private LocalDateTime timestamp;
    private String tipoOperazione;
    private int idUtenteCoinvolto;
    private String descrizione;

    public LogEntryBean() {
        // costruttore di default
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getTipoOperazione() {
        return tipoOperazione;
    }

    public void setTipoOperazione(String tipoOperazione) {
        this.tipoOperazione = tipoOperazione;
    }

    public int getIdUtenteCoinvolto() {
        return idUtenteCoinvolto;
    }

    public void setIdUtenteCoinvolto(int idUtenteCoinvolto) {
        this.idUtenteCoinvolto = idUtenteCoinvolto;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }
}
