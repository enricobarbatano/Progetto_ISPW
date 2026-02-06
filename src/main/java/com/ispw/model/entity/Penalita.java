package com.ispw.model.entity;


import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.ispw.model.enums.StatoPenalita;

public final class Penalita implements Serializable {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: entity penalita.
    // A2) IO: importo, motivazione, stato e data.
    // ========================
    private int idPenalita;
    private int idUtente; // FK
    private LocalDate dataEmissione;
    private BigDecimal importo;
    private String motivazione;
    private StatoPenalita stato;

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) getters/setters: accesso ai campi.
    // ========================

    public int getIdPenalita() { return idPenalita; }
    public void setIdPenalita(int idPenalita) { this.idPenalita = idPenalita; }

    public int getIdUtente() { return idUtente; }
    public void setIdUtente(int idUtente) { this.idUtente = idUtente; }

    public LocalDate getDataEmissione() { return dataEmissione; }
    public void setDataEmissione(LocalDate dataEmissione) { this.dataEmissione = dataEmissione; }

    public BigDecimal getImporto() { return importo; }
    public void setImporto(BigDecimal importo) { this.importo = importo; }

    public String getMotivazione() { return motivazione; }
    public void setMotivazione(String motivazione) { this.motivazione = motivazione; }

    public StatoPenalita getStato() { return stato; }
    public void setStato(StatoPenalita stato) { this.stato = stato; }
}
