package com.ispw.model.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

import com.ispw.model.enums.StatoPrenotazione;

public final class Prenotazione implements Serializable {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: entity prenotazione con riferimenti a campo/pagamento/fattura.
    // A2) IO: dati slot, stato e FK.
    // ========================
    private int idPrenotazione;

    private int idUtente; // FK utile per DB
    private int idCampo;  // FK utile per DB

    private LocalDate data;
    private LocalTime oraInizio;
    private LocalTime oraFine;
    private StatoPrenotazione stato;
    private boolean notificaRichiesta;

    // opzionali (popolati solo se necessari)
    private Campo campo;
    private Pagamento pagamento;
    private Fattura fattura;

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) getters/setters: accesso ai campi.
    // ========================

    public int getIdPrenotazione() { return idPrenotazione; }
    public void setIdPrenotazione(int idPrenotazione) { this.idPrenotazione = idPrenotazione; }

    public int getIdUtente() { return idUtente; }
    public void setIdUtente(int idUtente) { this.idUtente = idUtente; }

    public int getIdCampo() { return idCampo; }
    public void setIdCampo(int idCampo) { this.idCampo = idCampo; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getOraInizio() { return oraInizio; }
    public void setOraInizio(LocalTime oraInizio) { this.oraInizio = oraInizio; }

    public LocalTime getOraFine() { return oraFine; }
    public void setOraFine(LocalTime oraFine) { this.oraFine = oraFine; }

    public StatoPrenotazione getStato() { return stato; }
    public void setStato(StatoPrenotazione stato) { this.stato = stato; }

    public boolean isNotificaRichiesta() { return notificaRichiesta; }
    public void setNotificaRichiesta(boolean notificaRichiesta) { this.notificaRichiesta = notificaRichiesta; }

    public Campo getCampo() { return campo; }
    public void setCampo(Campo campo) { this.campo = campo; }

    public Pagamento getPagamento() { return pagamento; }
    public void setPagamento(Pagamento pagamento) { this.pagamento = pagamento; }

    public Fattura getFattura() { return fattura; }
    public void setFattura(Fattura fattura) { this.fattura = fattura; }
}
