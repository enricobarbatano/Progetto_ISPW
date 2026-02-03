package com.ispw.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.ispw.model.enums.TipoOperazione;

public final class SystemLog implements Serializable {
    private int idLog;
    private LocalDateTime timestamp;
    private TipoOperazione tipoOperazione;
    private int idUtenteCoinvolto;
    private String descrizione;

    public int getIdLog() { return idLog; }
    public void setIdLog(int idLog) { this.idLog = idLog; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public TipoOperazione getTipoOperazione() { return tipoOperazione; }
    public void setTipoOperazione(TipoOperazione tipoOperazione) { this.tipoOperazione = tipoOperazione; }

    public int getIdUtenteCoinvolto() { return idUtenteCoinvolto; }
    public void setIdUtenteCoinvolto(int idUtenteCoinvolto) { this.idUtenteCoinvolto = idUtenteCoinvolto; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
}
