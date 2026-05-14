package com.ispw.model.entity;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/*
 * Nel progetto coesistono entity anemiche e entity con comportamento:
 * le entity che modellano vincoli di dominio
 * (es. Campo con gestione degli slot) includono metodi per mantenere invarianti;
 * per altre entity la logica applicativa è orchestrata dai logic controller,
 * mantenendo i DAO come layer di persistenza e preservando DIP/SRP.
 */
public final class Campo implements Serializable {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: entity Campo con prenotazioni associate a runtime.
    // A2) IO: stato operativo e disponibilità.

    private int idCampo;
    private String nome;
    private String tipoSport;
    private Float costoOrario;
    private boolean isAttivo;
    private boolean flagManutenzione;

    /*
     * Lista runtime delle prenotazioni associate al campo.
     *
     * IMPORTANTE:
     * - Non viene serializzata in JSON grazie a @JsonIgnore.
     * - Non rappresenta lo stato persistente di Campo.
     * - Serve solo in memoria per i metodi di dominio, ad esempio isDisponibile(...).
     *
     * Le prenotazioni persistite vivono nel relativo DAO/file/tabella di Prenotazione.
     */
    @JsonIgnore
    private final List<Prenotazione> listaPrenotazioni = new ArrayList<>();

    // SEZIONE LOGICA
    // Legenda logica:
    // L1) getters/setters: dati anagrafici campo.
    // L2) isDisponibile/bloccoSlot/sbloccaSlot: gestione disponibilità.
    // L3) updateStatoOperativo/updateDisponibilitaCampo: stato operativo.

    public int getIdCampo() {
        return idCampo;
    }

    public void setIdCampo(int idCampo) {
        this.idCampo = idCampo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipoSport() {
        return tipoSport;
    }

    public void setTipoSport(String tipoSport) {
        this.tipoSport = tipoSport;
    }

    public Float getCostoOrario() {
        return costoOrario;
    }

    public void setCostoOrario(Float costoOrario) {
        this.costoOrario = costoOrario;
    }

    public boolean isAttivo() {
        return isAttivo;
    }

    public void setAttivo(boolean attivo) {
        isAttivo = attivo;
    }

    public boolean isFlagManutenzione() {
        return flagManutenzione;
    }

    public void setFlagManutenzione(boolean flagManutenzione) {
        this.flagManutenzione = flagManutenzione;
    }

    public List<Prenotazione> getListaPrenotazioni() {
        return Collections.unmodifiableList(listaPrenotazioni);
    }

    public void aggiungiPrenotazione(Prenotazione p) {
        if (p != null) {
            listaPrenotazioni.add(p);
        }
    }

    /**
     * Imposta le prenotazioni runtime associate al campo.
     *
     * Questo metodo viene usato dal layer applicativo/use case quando deve
     * verificare la disponibilità di un campo.
     *
     * Flusso previsto:
     * - CampoDAO carica il Campo puro.
     * - PrenotazioneDAO carica le prenotazioni attive associate al campo.
     * - Il logic controller inserisce temporaneamente tali prenotazioni nel Campo.
     * - Campo.isDisponibile(...) usa questa lista runtime per controllare conflitti.
     *
     * La lista viene prima svuotata per evitare duplicati nel caso in cui lo stesso
     * oggetto Campo venga arricchito più volte durante l'esecuzione.
     *
     * La lista non viene serializzata perché listaPrenotazioni è annotata con @JsonIgnore.
     *
     * @param prenotazioni lista di prenotazioni da usare solo a runtime
     */
    public void setPrenotazioniRuntime(List<Prenotazione> prenotazioni) {
        listaPrenotazioni.clear();

        if (prenotazioni == null) {
            return;
        }

        for (Prenotazione p : prenotazioni) {
            if (p != null) {
                listaPrenotazioni.add(p);
            }
        }
    }

    /**
     * Verifica se il campo è prenotabile nella finestra richiesta.
     *
     * Regole:
     * 1) Se il campo non è attivo o è in manutenzione, non è disponibile.
     * 2) Se non ci sono prenotazioni runtime caricate, si assume nessun conflitto.
     * 3) Altrimenti, si controlla che l'intervallo richiesto non si sovrapponga
     *    a prenotazioni già presenti nello stesso giorno.
     *
     * @param data      giorno richiesto
     * @param oraInizio ora inizio richiesta
     * @param oraFine   ora fine richiesta; se null viene trattata come slot puntuale
     * @return true se disponibile, false altrimenti
     */
    public boolean isDisponibile(Date data, Time oraInizio, Time oraFine) {
        if (!isAttivo || flagManutenzione) {
            return false;
        }

        LocalDate giorno = data.toLocalDate();
        LocalTime start = oraInizio.toLocalTime();
        LocalTime end = (oraFine != null) ? oraFine.toLocalTime() : start;

        if (listaPrenotazioni.isEmpty()) {
            return true;
        }

        for (Prenotazione p : listaPrenotazioni) {
            if (p.getData() == null || p.getOraInizio() == null || !giorno.equals(p.getData())) {
                continue;
            }

            LocalTime pStart = p.getOraInizio();
            LocalTime pEnd = (p.getOraFine() != null) ? p.getOraFine() : pStart;

            /*
             * Regola di sovrapposizione intervalli [start, end):
             * due intervalli si sovrappongono se:
             *
             * start < pEnd && pStart < end
             */
            boolean overlap = start.isBefore(pEnd) && pStart.isBefore(end);
            if (overlap) {
                return false;
            }
        }

        return true;
    }

    /**
     * Blocca uno slot aggiungendo una prenotazione tecnica in memoria.
     *
     * Nota:
     * questa operazione modifica solo la lista runtime del Campo.
     * Non persiste una prenotazione: la persistenza reale dello slot deve passare
     * da PrenotazioneDAO.store(...).
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
     * Sblocca uno slot rimuovendo dalla lista runtime la prenotazione
     * con stessa data e stessa ora di inizio.
     *
     * Nota:
     * questa operazione modifica solo lo stato runtime del Campo.
     * La persistenza dello stato della prenotazione deve essere gestita tramite
     * PrenotazioneDAO.updateStato(...).
     */
    public void sbloccaSlot(Date data, Time oraInizio) {
        LocalDate d = data.toLocalDate();
        LocalTime t = oraInizio.toLocalTime();

        listaPrenotazioni.removeIf(p ->
            d.equals(p.getData()) && t.equals(p.getOraInizio())
        );
    }

    /**
     * Aggiorna lo stato operativo del campo.
     */
    public void updateStatoOperativo(boolean isAttivo, boolean flagManutenzione) {
        this.isAttivo = isAttivo;
        this.flagManutenzione = flagManutenzione;
    }

    /**
     * Wrapper per uniformarsi a possibili chiamate testuali, ad esempio da CLI.
     *
     * azione = "BLOCCA"  -> blocco puntuale runtime
     * azione = "SBLOCCA" -> sblocco runtime
     */
    public void updateDisponibilitaCampo(int idCampo, Date data, Time time, String azione) {
        if (idCampo != this.idCampo) {
            return;
        }

        if ("SBLOCCA".equalsIgnoreCase(azione)) {
            sbloccaSlot(data, time);
        } else if ("BLOCCA".equalsIgnoreCase(azione)) {
            bloccoSlot(data, time, null);
        }
    }
}