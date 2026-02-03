package com.ispw.bean;



import java.math.BigDecimal;
import java.time.LocalDate;

public class DatiPenalitaBean  {
   

    private int idUtente;
    private BigDecimal importo;
    private String motivazione;
    private LocalDate dataDecorrenza;

    public DatiPenalitaBean() {}

    public int getIdUtente() { return idUtente; }
    public void setIdUtente(int idUtente) { this.idUtente = idUtente; }

    public BigDecimal getImporto() { return importo; }
    public void setImporto(BigDecimal importo) { this.importo = importo; }

    public String getMotivazione() { return motivazione; }
    public void setMotivazione(String motivazione) { this.motivazione = motivazione; }

    public LocalDate getDataDecorrenza() { return dataDecorrenza; }
    public void setDataDecorrenza(LocalDate dataDecorrenza) { this.dataDecorrenza = dataDecorrenza; }
}
