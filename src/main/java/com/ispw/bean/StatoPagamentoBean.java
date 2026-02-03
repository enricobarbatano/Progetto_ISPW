package com.ispw.bean;


import java.time.LocalDateTime;

public class StatoPagamentoBean  {

    private boolean successo;
    private String stato;            // Enum StatoPagamento nel Model
    private String idTransazione;
    private LocalDateTime dataPagamento;
    private String messaggio;        // eventuale descrizione/esito gateway

    public StatoPagamentoBean() {
        //Nota: costruttore no-args intenzionalmente vuoto.
    }

    public boolean isSuccesso() { return successo; }
    public void setSuccesso(boolean successo) { this.successo = successo; }

    public String getStato() { return stato; }
    public void setStato(String stato) { this.stato = stato; }

    public String getIdTransazione() { return idTransazione; }
    public void setIdTransazione(String idTransazione) { this.idTransazione = idTransazione; }

    public LocalDateTime getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDateTime dataPagamento) { this.dataPagamento = dataPagamento; }

    public String getMessaggio() { return messaggio; }
    public void setMessaggio(String messaggio) { this.messaggio = messaggio; }
}
