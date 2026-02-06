package com.ispw.model.entity;


import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ispw.model.enums.MetodoPagamento;
import com.ispw.model.enums.StatoPagamento;

public final class Pagamento implements Serializable {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: entity pagamento.
    // A2) IO: importo, metodo, stato e data.
    // ========================
    private int idPagamento;
    private int idPrenotazione; // FK
    private BigDecimal importoFinale;
    private MetodoPagamento metodo;
    private StatoPagamento stato;
    private LocalDateTime dataPagamento;

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) getters/setters: accesso ai campi.
    // ========================

    public int getIdPagamento() { return idPagamento; }
    public void setIdPagamento(int idPagamento) { this.idPagamento = idPagamento; }

    public int getIdPrenotazione() { return idPrenotazione; }
    public void setIdPrenotazione(int idPrenotazione) { this.idPrenotazione = idPrenotazione; }

    public BigDecimal getImportoFinale() { return importoFinale; }
    public void setImportoFinale(BigDecimal importoFinale) { this.importoFinale = importoFinale; }

    public MetodoPagamento getMetodo() { return metodo; }
    public void setMetodo(MetodoPagamento metodo) { this.metodo = metodo; }

    public StatoPagamento getStato() { return stato; }
    public void setStato(StatoPagamento stato) { this.stato = stato; }

    public LocalDateTime getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDateTime dataPagamento) { this.dataPagamento = dataPagamento; }
}
