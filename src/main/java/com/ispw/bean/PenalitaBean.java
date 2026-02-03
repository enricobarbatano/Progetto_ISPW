package com.ispw.bean;

import java.math.BigDecimal;

public class PenalitaBean  {
    

    private BigDecimal valorePenalita;
    private int preavvisoMinimoMinuti;

    public PenalitaBean() {}

    public BigDecimal getValorePenalita() { return valorePenalita; }
    public void setValorePenalita(BigDecimal valorePenalita) { this.valorePenalita = valorePenalita; }

    public int getPreavvisoMinimoMinuti() { return preavvisoMinimoMinuti; }
    public void setPreavvisoMinimoMinuti(int preavvisoMinimoMinuti) { this.preavvisoMinimoMinuti = preavvisoMinimoMinuti; }
}
