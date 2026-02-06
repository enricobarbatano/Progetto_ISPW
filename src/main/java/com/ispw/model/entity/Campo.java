package com.ispw.model.entity;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Campo implements Serializable {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: entity campo con prenotazioni associate.
    // A2) IO: stato operativo e disponibilita'.
    // ========================

    private int idCampo;
    private String nome;
    private String tipoSport;
    private Float costoOrario;       // es. 20.0f = 20€/h
    private boolean isAttivo;        // se false, non è prenotabile
    private boolean flagManutenzione;// se true, non è prenotabile

   
    private final List<Prenotazione> listaPrenotazioni = new ArrayList<>();

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) getters/setters: dati anagrafici campo.
    // L2) isDisponibile/bloccoSlot/sbloccaSlot: gestione disponibilita'.
    // L3) updateStatoOperativo/updateDisponibilitaCampo: stato operativo.
    // ========================

  
    public int getIdCampo() { return idCampo; }
    public void setIdCampo(int idCampo) { this.idCampo = idCampo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTipoSport() { return tipoSport; }
    public void setTipoSport(String tipoSport) { this.tipoSport = tipoSport; }

    public Float getCostoOrario() { return costoOrario; }
    public void setCostoOrario(Float costoOrario) { this.costoOrario = costoOrario; }

    public boolean isAttivo() { return isAttivo; }
    public void setAttivo(boolean attivo) { isAttivo = attivo; }

    public boolean isFlagManutenzione() { return flagManutenzione; }
    public void setFlagManutenzione(boolean flagManutenzione) { this.flagManutenzione = flagManutenzione; }

    public List<Prenotazione> getListaPrenotazioni() {
        return Collections.unmodifiableList(listaPrenotazioni);
    }

    public void aggiungiPrenotazione(Prenotazione p) {
        if (p != null) {
            listaPrenotazioni.add(p);
        }
    }

    /**
     * Verifica se il campo è prenotabile nella finestra richiesta.
     * Regole semplici:
     * 1) Se non è attivo o è in manutenzione -> NON disponibile.
     * 2) Se non abbiamo prenotazioni caricate -> non possiamo verificare conflitti, quindi OK.
     * 3) Altrimenti, controlliamo che l'intervallo non si sovrapponga a prenotazioni già esistenti (stesso giorno).
     *
     * @param data      giorno richiesto (java.sql.Date)
     * @param oraInizio ora inizio richiesta (java.sql.Time)
     * @param oraFine   ora fine richiesta (può essere null -> slot "puntuale")
     * @return true se disponibile, false altrimenti
     */
    public boolean isDisponibile(Date data, Time oraInizio, Time oraFine) {
        // Stato operativo del campo
        if (!isAttivo || flagManutenzione) return false;

        // Conversione in tipi java.time (più semplici da confrontare)
        LocalDate giorno = data.toLocalDate();
        LocalTime start  = oraInizio.toLocalTime();
        LocalTime end    = (oraFine != null ? oraFine.toLocalTime() : start); // se null, uso start (slot istantaneo)

        // Se non abbiamo prenotazioni in memoria, assumiamo nessun conflitto
        if (listaPrenotazioni.isEmpty()) return true;

        // Controllo conflitti con prenotazioni dello stesso giorno
        for (Prenotazione p : listaPrenotazioni) {
            if (p.getData() == null || p.getOraInizio() == null || !giorno.equals(p.getData())) continue;

            LocalTime pStart = p.getOraInizio();
            LocalTime pEnd   = (p.getOraFine() != null ? p.getOraFine() : pStart);

            // Regola di sovrapposizione (intervalli [start, end)):
            // si sovrappongono se start < pEnd E pStart < end
            boolean overlap = start.isBefore(pEnd) && pStart.isBefore(end);
            if (overlap) return false;
        }
        return true;
    }

    /**
     * "Blocca" uno slot aggiungendo una prenotazione tecnica in memoria.
     * Utile per evitare doppie prenotazioni nello stesso istante.
     */
    public void bloccoSlot(Date data, Time oraInizio, Time oraFine) {
        Prenotazione p = new Prenotazione();
        p.setIdCampo(this.idCampo);
        p.setCampo(this);
        p.setData(data.toLocalDate());
        p.setOraInizio(oraInizio.toLocalTime());
        p.setOraFine(oraFine != null ? oraFine.toLocalTime() : null);
        aggiungiPrenotazione(p);
    }

    /**
     * "Sblocca" lo slot: rimuove la prenotazione (o il blocco tecnico) che
     * ha la stessa data e la stessa ora di inizio.
     */
    public void sbloccaSlot(Date data, Time oraInizio) {
        LocalDate d = data.toLocalDate();
        LocalTime t = oraInizio.toLocalTime();

        listaPrenotazioni.removeIf(p ->
            d.equals(p.getData()) && t.equals(p.getOraInizio())
        );
    }

    /**
     * Aggiorna lo stato operativo (attivo/manutenzione).
     */
    public void updateStatoOperativo(boolean isAttivo, boolean flagManutenzione) {
        this.isAttivo = isAttivo;
        this.flagManutenzione = flagManutenzione;
    }

    /**
     * Piccolo "wrapper" per uniformarsi a possibili chiamate testuali (es. da CLI):
     * azione = "BLOCCA" -> blocco puntuale
     * azione = "SBLOCCA" -> sblocco
     * (Puoi estenderlo con "ATTIVA"/"DISATTIVA" se serve)
     */
    public void updateDisponibilitaCampo(int idCampo, Date data, Time time, String azione) {
        if (idCampo != this.idCampo) return;

        if ("SBLOCCA".equalsIgnoreCase(azione)) {
            sbloccaSlot(data, time);
        } else if ("BLOCCA".equalsIgnoreCase(azione)) {
            bloccoSlot(data, time, null);
        }
    }
}