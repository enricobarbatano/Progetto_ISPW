package com.ispw.model.entity;

import java.io.Serializable;
import java.math.BigDecimal;

public final class RegolePenalita implements Serializable {

    private int idConfig = 1;

    private BigDecimal valorePenalita; // importo base (o %), dipende dalle tue regole
    private int preavvisoMinimo;       // minuti

    public RegolePenalita() { /* default ctor for mapping/serialization */ }

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
