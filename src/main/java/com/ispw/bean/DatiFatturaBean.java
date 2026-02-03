package com.ispw.bean;



import java.math.BigDecimal;
import java.time.LocalDate;

public class DatiFatturaBean  {
   

    private String codiceFiscaleCliente;
    private String intestatario;
    private String indirizzo;
    private String email;
    private String partitaIva;
    private BigDecimal importo;
    private LocalDate dataOperazione;

    public DatiFatturaBean() {
        //Nota: costruttore no-args intenzionalmente vuoto.
    }

    public String getCodiceFiscaleCliente() { return codiceFiscaleCliente; }
    public void setCodiceFiscaleCliente(String codiceFiscaleCliente) { this.codiceFiscaleCliente = codiceFiscaleCliente; }

    public String getIntestatario() { return intestatario; }
    public void setIntestatario(String intestatario) { this.intestatario = intestatario; }

    public String getIndirizzo() { return indirizzo; }
    public void setIndirizzo(String indirizzo) { this.indirizzo = indirizzo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPartitaIva() { return partitaIva; }
    public void setPartitaIva(String partitaIva) { this.partitaIva = partitaIva; }

    public BigDecimal getImporto() { return importo; }
    public void setImporto(BigDecimal importo) { this.importo = importo; }

    public LocalDate getDataOperazione() { return dataOperazione; }
    public void setDataOperazione(LocalDate dataOperazione) { this.dataOperazione = dataOperazione; }
}
