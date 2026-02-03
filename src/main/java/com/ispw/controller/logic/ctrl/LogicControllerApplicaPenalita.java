package com.ispw.controller.logic.ctrl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.DatiFatturaBean;// passato come parametro al controller secondario per il metodo, non ha influenza diretta sulla logica di business in questa classe
import com.ispw.bean.DatiPagamentoBean;// passato come parametro al controller secondario per il metodo, non ha influenza diretta sulla logica di business in questa classe
import com.ispw.bean.DatiPenalitaBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.controller.logic.interfaces.fattura.GestioneFatturaPenalita;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaPenalita;
import com.ispw.controller.logic.interfaces.pagamento.GestionePagamentoPenalita;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.dao.interfaces.PenalitaDAO;
import com.ispw.dao.interfaces.RegolePenalitaDAO;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.entity.Penalita;
import com.ispw.model.entity.RegolePenalita;
import com.ispw.model.entity.SystemLog;

/**
 * Controller applicativo per l'applicazione di penalità.
 * - Nessun SQL qui dentro: uso solo i DAO della factory.
 * - DIP: i collaboratori (pagamento, fattura, notifica) sono passati via parametri nell'overload.
 * - Statless + Sonar-friendly (logger on-demand, early-return, messaggi centralizzati).
 */

public class LogicControllerApplicaPenalita {

    // ========================
    // Messaggi (no magic strings)
    // ========================
    private static final String MSG_INPUT_NON_VALIDO   = "Dati penalità non validi";
    private static final String MSG_UTENTE_INESISTENTE = "Utente inesistente";
    private static final String MSG_IMPORTO_NON_VALIDO = "Importo penalità non valido";
    private static final String MSG_OK                 = "Penalità applicata";

    // ========================
    // Logger on-demand (S1312)
    // ========================
    @SuppressWarnings("java:S1312")
    private Logger log() { return Logger.getLogger(getClass().getName()); }

    // ========================
    // DAO (no accoppiamento a concreti)
    // ========================
    private GeneralUserDAO    userDAO()    { return DAOFactory.getInstance().getGeneralUserDAO(); }
    private RegolePenalitaDAO rulesDAO()   { return DAOFactory.getInstance().getRegolePenalitaDAO(); }
    private PenalitaDAO       penalitaDAO(){ return DAOFactory.getInstance().getPenalitaDAO(); }
    private LogDAO            logDAO()     { return DAOFactory.getInstance().getLogDAO(); }

    // =====================================================================================
    // Firma richiesta dal caso d'uso
    // =====================================================================================
    /**
     * Applica una sanzione con la logica minima (validazione, default importo da regole,
     * logging e notifica se presente un orchestratore esterno), SENZA orchestrare pagamento/fattura.
     *
     * Per integrare pagamento e fattura, usare l'overload che accetta i collaboratori secondari.
     */
    public EsitoOperazioneBean applicaSanzione(DatiPenalitaBean dati) {
        // validazioni base
        if (!isValid(dati)) {
            return esito(false, MSG_INPUT_NON_VALIDO);
        }
        final int idUtente = dati.getIdUtente();
        final GeneralUser user = userDAO().findById(idUtente);
        if (user == null) {
            return esito(false, MSG_UTENTE_INESISTENTE);
        }

        // importo effettivo (bean -> regole -> KO)
        final BigDecimal importo = resolveImportoOrDefault(dati);
        if (importo == null || importo.compareTo(BigDecimal.ZERO) <= 0) {
            return esito(false, MSG_IMPORTO_NON_VALIDO);
        }

        // Persistenza best-effort su PenalitaDAO (con fallback deterministico)
        final int idPenalita = persistOrComputeId(dati);

        // logging best-effort
        appendLogSafe(idUtente, "[PENALITA] id=" + idPenalita + " importo=" + importo + " motivo=" + safe(dati.getMotivazione()));

        // notifica opzionale demandata a orchestratore esterno
        // (qui volutamente non invochiamo GestioneNotificaPenalita per mantenere la firma singola)
        return esito(true, MSG_OK + " (idPenalita=" + idPenalita + ")");
    }

    // =====================================================================================
    // Overload con DIP (pagamento/fattura/notifica passati come parametri)
    // =====================================================================================
    /**
     * Overload che integra il flusso completo: validazione → (default importo) → notifica → pagamento → fattura → log.
     * Mantiene il controller stateless e testabile (fake dei collaboratori nei test).
     */
    public EsitoOperazioneBean applicaSanzione(DatiPenalitaBean dati,
                                               DatiPagamentoBean datiPagamento,
                                               DatiFatturaBean   datiFattura,
                                               GestionePagamentoPenalita payCtrl,
                                               GestioneFatturaPenalita   fattCtrl,
                                               GestioneNotificaPenalita  notiCtrl) {
        if (!isValid(dati)) {
            return esito(false, MSG_INPUT_NON_VALIDO);
        }
        final int idUtente = dati.getIdUtente();
        final GeneralUser user = userDAO().findById(idUtente);
        if (user == null) {
            return esito(false, MSG_UTENTE_INESISTENTE);
        }

        final BigDecimal importo = resolveImportoOrDefault(dati);
        if (importo == null || importo.compareTo(BigDecimal.ZERO) <= 0) {
            return esito(false, MSG_IMPORTO_NON_VALIDO);
        }

        // Persistenza best-effort su PenalitaDAO (con fallback deterministico)
        final int idPenalita = persistOrComputeId(dati);

        // notifica (se fornita)
        if (notiCtrl != null) {
            try {
                notiCtrl.inviaNotificaPenalita(String.valueOf(idUtente));
            } catch (RuntimeException ex) {
                log().log(Level.FINE, "Notifica penalità fallita: " + ex.getMessage(), ex);
            }
        }

        // pagamento (se richiesto)
        if (payCtrl != null && datiPagamento != null) {
            // se l'importo del DTO è 0 o mancante, usa quello della penalità
            if (datiPagamento.getImporto() <= 0f) {
                datiPagamento.setImporto(importo.floatValue());
            }
            // se il metodo non è specificato, imponiamo un default sicuro
            if (isBlank(datiPagamento.getMetodo())) {
                datiPagamento.setMetodo("PAYPAL");
            }
            try {
                payCtrl.richiediPagamentoPenalità(datiPagamento, idPenalita);
            } catch (RuntimeException ex) {
                log().log(Level.FINE, "Pagamento penalità fallito: " + ex.getMessage(), ex);
            }
        }

        // fattura (se richiesta)
        if (fattCtrl != null && datiFattura != null) {
            // se la data non è indicata, la impostiamo ad oggi (evita null nel generatore)
            if (datiFattura.getDataOperazione() == null) {
                datiFattura.setDataOperazione(LocalDate.now());
            }
            try {
                fattCtrl.generaFatturaPenalita(datiFattura, idPenalita);
            } catch (RuntimeException ex) {
                log().log(Level.FINE, "Fattura penalità fallita: " + ex.getMessage(), ex);
            }
        }

        appendLogSafe(idUtente, "[PENALITA] id=" + idPenalita + " importo=" + importo + " motivo=" + safe(dati.getMotivazione()));
        return esito(true, MSG_OK + " (idPenalita=" + idPenalita + ")");
    }

    // ========================
    // Helper (validazioni, calcoli, log, persistenza)
    // ========================
    private boolean isValid(DatiPenalitaBean d) {
        // forma condensata per eliminare l'if ridondante segnalato da Sonar
        return d != null
            && d.getIdUtente() > 0
            && !isBlank(d.getMotivazione());
        // dataDecorrenza è facoltativa; se null, la tratteremo come oggi nelle note/descrizioni
    }

    private BigDecimal resolveImportoOrDefault(DatiPenalitaBean d) {
        if (d.getImporto() != null && d.getImporto().compareTo(BigDecimal.ZERO) > 0) {
            return d.getImporto();
        }
        final RegolePenalita r = rulesDAO().get();
        if (r != null && r.getValorePenalita() != null && r.getValorePenalita().compareTo(BigDecimal.ZERO) > 0) {
            return r.getValorePenalita();
        }
        return null;
    }

    /**
     * Prova a persistere la penalità su PenalitaDAO.
     * - Se il provider non supporta ancora la persistenza, logga a livello FINE e usa un ID deterministico come fallback.
     * - Per i provider senza auto-increment, assegna un ID deterministico PRIMA dello store per garantire una chiave non-zero.
     */
    private int persistOrComputeId(DatiPenalitaBean d) {
        final int deterministicId = computeIdPenalitaDeterministico(d);
        try {
            final Penalita p = new Penalita();
            // Assegno un ID non-zero per provider senza auto-increment (es. FileSystem)
            p.setIdPenalita(deterministicId);
            p.setIdUtente(d.getIdUtente());
            // Se in futuro l'entity esponesse campi aggiuntivi (importo/motivazione/data), valorizzali qui.

            penalitaDAO().store(p);
            // In caso di provider con ID generato, qui potresti rileggere l'entità per ottenere l'ID reale.
            // Al momento restituiamo l'ID impostato (coerente cross-provider).
            return p.getIdPenalita();
        } catch (UnsupportedOperationException ex) {
            log().log(Level.FINE, "Provider penalitaDAO non supporta ancora store(): " + ex.getMessage(), ex);
            return deterministicId;
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Persistenza penalità fallita: " + ex.getMessage(), ex);
            return deterministicId;
        }
    }

    /** ID deterministico, adatto a correlare pagamento/fattura quando non si dispone ancora del DAO specifico. */
    private int computeIdPenalitaDeterministico(DatiPenalitaBean d) {
        int h = Objects.hash(
                d.getIdUtente(),
                d.getMotivazione() != null ? d.getMotivazione().trim() : "",
                d.getDataDecorrenza() != null ? d.getDataDecorrenza() : LocalDate.now(),
                d.getImporto() != null ? d.getImporto() : BigDecimal.ZERO
        );
        // garantisco > 0
        return Math.abs(h == Integer.MIN_VALUE ? h + 1 : h);
    }

    private String safe(String s) { return (s == null) ? "" : s; }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private EsitoOperazioneBean esito(boolean ok, String msg) {
        EsitoOperazioneBean e = new EsitoOperazioneBean();
        e.setSuccess(ok);
        e.setMessaggio(msg);
        return e;
    }

    private void appendLogSafe(int idUtente, String descr) {
        try {
            SystemLog l = new SystemLog();
            l.setTimestamp(LocalDateTime.now());
            l.setIdUtenteCoinvolto(idUtente);
            l.setDescrizione(descr);
            logDAO().append(l);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Append log penalità fallito: " + ex.getMessage(), ex);
        }
    }
}
