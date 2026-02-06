package com.ispw.model.entity;

import java.io.Serializable;
import java.math.BigDecimal;

public final class RegolePenalita implements Serializable {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: entity regole penalita.
    // A2) IO: configurazione importo e preavviso.
    // ========================

    private int idConfig = 1;

    private BigDecimal valorePenalita;
    private int preavvisoMinimo;

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) costruttori: mapping/serializzazione.
    // L2) getters/setters: accesso ai campi.
    // ========================

    public RegolePenalita() { }

    public RegolePenalita(BigDecimal valorePenalita, int preavvisoMinimo) {
        this.valorePenalita = valorePenalita;
        this.preavvisoMinimo = preavvisoMinimo;
    }

    public int getIdConfig() { return idConfig; }
    public void setIdConfig(int idConfig) { this.idConfig = idConfig; }

    public BigDecimal getValorePenalita() { return valorePenalita; }
    public void setValorePenalita(BigDecimal valorePenalita) { this.valorePenalita = valorePenalita; }

    public int getPreavvisoMinimo() { return preavvisoMinimo; }
    public void setPreavvisoMinimo(int preavvisoMinimo) { this.preavvisoMinimo = preavvisoMinimo; }
}
