package com.ispw.controller.logic.ctrl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.DatiPagamentoBean;
import com.ispw.bean.StatoPagamentoBean;
import com.ispw.controller.logic.interfaces.pagamento.GestionePagamentoDisdetta;
import com.ispw.controller.logic.interfaces.pagamento.GestionePagamentoPenalita;
import com.ispw.controller.logic.interfaces.pagamento.GestionePagamentoPrenotazione;
import com.ispw.controller.logic.interfaces.pagamento.GestionePagamentoRimborso;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.PagamentoDAO;
import com.ispw.model.entity.Pagamento;
import com.ispw.model.enums.MetodoPagamento;
import com.ispw.model.enums.StatoPagamento;

/**
 * Controller applicativo secondario per la gestione dei pagamenti.
 *
 * Questo controller viene usato dai controller principali per:
 * - richiedere un pagamento legato a una prenotazione;
 * - recuperare l'importo pagato in fase di disdetta;
 * - eseguire un rimborso;
 * - richiedere un pagamento legato a una penalità.
 *
 * Nota di progetto:
 * questa classe contiene la logica tecnica del pagamento simulato.
 * I controller principali non devono conoscere i dettagli del pagamento,
 * non devono accedere direttamente al PagamentoDAO e non devono costruire
 * direttamente lo StatoPagamentoBean.
 */
public class LogicControllerGestionePagamento
        implements GestionePagamentoPrenotazione,
                   GestionePagamentoRimborso,
                   GestionePagamentoPenalita,
                   GestionePagamentoDisdetta {

    private static final String PAGATO = "PAGATO";
    private static final String FALLITO = "FALLITO";
    private static final String RIFIUTATO = "RIFIUTATO";
    private static final String NEGATO = "NEGATO";
    private static final String KO = "KO";

    @SuppressWarnings("java:S1312")
    private Logger log() {
        return Logger.getLogger(getClass().getName());
    }

    // =====================================================================
    // DAO
    // =====================================================================
    // Il DAO viene recuperato dalla DAOFactory.
    // In questo modo il controller lavora sull'interfaccia e non conosce
    // l'implementazione concreta della persistenza.

    private PagamentoDAO pagamentoDAO() {
        return DAOFactory.getInstance().getPagamentoDAO();
    }

    // STEP 1: pagamento prenotazione

    /**
     * Richiede il pagamento di una prenotazione.
     *
     * Il metodo:
     * - valida i dati di pagamento;
     * - recupera o crea il pagamento associato alla prenotazione;
     * - simula il gateway di pagamento;
     * - salva il pagamento;
     * - restituisce uno StatoPagamentoBean completo.
     */
    @Override
    public StatoPagamentoBean richiediPagamentoPrenotazione(DatiPagamentoBean dati, int idPrenotazione) {
        //controllo che i dati pagamento siano presenti
        if (dati == null) {
            return esito(false, statoKo().name(), null, "Dati pagamento mancanti");
        }

        //controllo che il metodo pagamento sia valido
        MetodoPagamento metodo = parseMetodo(dati.getMetodo());
        if (metodo == null) {
            return esito(false, statoKo().name(), null, "Metodo pagamento non valido");
        }

        //recupero eventuale pagamento già presente per la prenotazione
        Pagamento pay = pagamentoDAO().findByPrenotazione(idPrenotazione);
        if (pay == null) {
            pay = new Pagamento();
            pay.setIdPrenotazione(idPrenotazione);
        }

        //popolo i dati minimi del pagamento
        float importo = Math.max(0f, dati.getImporto());
        pay.setImportoFinale(BigDecimal.valueOf(importo));
        pay.setMetodo(metodo);
        pay.setDataPagamento(LocalDateTime.from(ZonedDateTime.now(ZoneId.systemDefault())));

        /*
         * Gateway fittizio:
         * se l'importo è maggiore di zero, il pagamento viene considerato ok.
         */
        boolean ok = importo > 0f;
        StatoPagamento stato = ok ? statoOk() : statoKo();

        pay.setStato(stato);
        pagamentoDAO().store(pay);

        log().log(Level.FINE, "[PAGAMENTO] Prenotazione #{0}, stato={1}",
                new Object[]{idPrenotazione, stato});

        return buildEsitoFromPagamento(pay, ok);
    }

    // STEP 2: consultazione pagamento per disdetta

    /**
     * Recupera l'importo effettivamente pagato per una prenotazione.
     *
     * Questo metodo viene usato dal caso d'uso disdetta per stimare
     * il rimborso, senza far dipendere il controller disdetta dal PagamentoDAO.
     */
    @Override
    public float recuperaImportoPagato(int idPrenotazione) {
        if (idPrenotazione <= 0) {
            return 0f;
        }

        Pagamento pagamento = pagamentoDAO().findByPrenotazione(idPrenotazione);
        if (pagamento == null || pagamento.getImportoFinale() == null) {
            return 0f;
        }

        return Math.max(0f, pagamento.getImportoFinale().floatValue());
    }

    // STEP 3: rimborso prenotazione

    /**
     * Esegue il rimborso di una prenotazione.
     *
     * Il metodo aggiorna lo stato del pagamento associato alla prenotazione.
     * Se il pagamento non esiste o l'importo non è valido, il flusso termina.
     */
    @Override
    public void eseguiRimborso(int idPrenotazione, float importo) {
        Pagamento pay = pagamentoDAO().findByPrenotazione(idPrenotazione);
        if (pay == null || importo <= 0f) {
            return;
        }

        //stato rimborso: prova alias comuni, altrimenti fallback
        StatoPagamento statoRimborso = pickStato("RIMBORSATO", "REFUNDED");

        pay.setStato(statoRimborso);
        pay.setDataPagamento(LocalDateTime.from(ZonedDateTime.now(ZoneId.systemDefault())));
        pagamentoDAO().store(pay);
    }

    // STEP 4: pagamento penalità

    /**
     * Richiede il pagamento di una penalità.
     *
     * Nota:
     * in questa versione scolastica il pagamento della penalità viene legato
     * a un idPrenotazione negativo, ricavato dall'id penalità.
     */
    @Override
    public StatoPagamentoBean richiediPagamentoPenalita(DatiPagamentoBean dati, int idPenalita) {
        //controllo che i dati pagamento siano presenti
        if (dati == null) {
            return esito(false, statoKo().name(), null, "Dati pagamento mancanti");
        }

        //controllo che il metodo pagamento sia valido
        MetodoPagamento metodo = parseMetodo(dati.getMetodo());
        if (metodo == null) {
            return esito(false, statoKo().name(), null, "Metodo pagamento non valido");
        }

        /*
         * Soluzione scolastica:
         * il pagamento della penalità viene salvato usando idPrenotazione negativo.
         */
        Pagamento pay = new Pagamento();
        pay.setIdPrenotazione(-Math.abs(idPenalita));

        float importo = Math.max(0f, dati.getImporto());
        pay.setImportoFinale(BigDecimal.valueOf(importo));
        pay.setMetodo(metodo);
        pay.setDataPagamento(LocalDateTime.from(ZonedDateTime.now(ZoneId.systemDefault())));

        boolean ok = importo > 0f;
        StatoPagamento stato = ok ? statoOk() : statoKo();

        pay.setStato(stato);
        pagamentoDAO().store(pay);

        return esito(ok,
                stato.name(),
                newTxId("PX"),
                ok ? "Pagamento penalita eseguito" : "Pagamento penalita rifiutato");
    }

    // =====================================================================
    // COSTRUZIONE ESITO PAGAMENTO
    // =====================================================================

    /**
     * Costruisce l'esito pagamento a partire dal pagamento persistito.
     */
    private StatoPagamentoBean buildEsitoFromPagamento(Pagamento pagamento, boolean success) {
        if (pagamento == null) {
            return esito(false, statoKo().name(), null, "Pagamento rifiutato");
        }

        StatoPagamentoBean bean = new StatoPagamentoBean();
        bean.setSuccesso(success);

        if (pagamento.getStato() != null) {
            bean.setStato(pagamento.getStato().name());
        } else {
            bean.setStato(resolveStatoPagamentoFallback(success));
        }

        bean.setIdTransazione(newTxId("PX"));
        bean.setDataPagamento(pagamento.getDataPagamento());
        bean.setMessaggio(resolveMessaggioPagamento(success));

        return bean;
    }

    /**
     * Costruisce un esito pagamento standard.
     */
    private StatoPagamentoBean esito(boolean ok, String stato, String idTx, String msg) {
        StatoPagamentoBean bean = new StatoPagamentoBean();
        bean.setSuccesso(ok);
        bean.setStato(resolveStatoPagamentoOutput(stato, ok));
        bean.setIdTransazione(resolveTxId(idTx));
        bean.setDataPagamento(LocalDateTime.from(ZonedDateTime.now(ZoneId.systemDefault())));
        bean.setMessaggio(msg);

        return bean;
    }

    /**
     * Risolve lo stato testuale in base al successo del pagamento.
     */
    private String resolveStatoPagamentoFallback(boolean success) {
        if (success) {
            return PAGATO;
        }

        return KO;
    }

    /**
     * Risolve lo stato da inserire nell'output.
     *
     * Se lo stato è già presente, viene usato quello.
     * Altrimenti viene calcolato in base all'esito.
     */
    private String resolveStatoPagamentoOutput(String stato, boolean ok) {
        if (stato != null) {
            return stato;
        }

        return resolveStatoPagamentoFallback(ok);
    }

    /**
     * Risolve il messaggio di pagamento in base all'esito.
     */
    private String resolveMessaggioPagamento(boolean success) {
        if (success) {
            return "Pagamento eseguito";
        }

        return "Pagamento rifiutato";
    }

    /**
     * Risolve l'id transazione.
     *
     * Se è già presente viene usato quello, altrimenti viene generato.
     */
    private String resolveTxId(String idTx) {
        if (idTx != null) {
            return idTx;
        }

        return newTxId("TX");
    }

    /**
     * Genera un id transazione semplice usando un prefisso e il timestamp corrente.
     */
    private String newTxId(String prefix) {
        return prefix + "-" + System.currentTimeMillis();
    }

    // =====================================================================
    // HELPERS PAGAMENTO
    // =====================================================================

    /**
     * Converte il nome del metodo pagamento nell'enum MetodoPagamento.
     */
    private MetodoPagamento parseMetodo(String nome) {
        if (nome == null) {
            return null;
        }

        try {
            return MetodoPagamento.valueOf(nome.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Prova alias per ricavare uno StatoPagamento reale.
     *
     * Se nessuno degli alias esiste nell'enum, usa il primo valore disponibile.
     */
    private StatoPagamento pickStato(String... preferiti) {
        for (String s : preferiti) {
            try {
                return StatoPagamento.valueOf(s);
            } catch (IllegalArgumentException ex) {
                log().log(Level.FINE, "Alias StatoPagamento non trovato: {0}", s);
            }
        }

        StatoPagamento[] valori = StatoPagamento.values();
        if (valori.length > 0) {
            return valori[0];
        }

        return null;
    }

    /**
     * Risolve lo stato positivo del pagamento.
     */
    private StatoPagamento statoOk() {
        StatoPagamento stato = pickStato("OK", "ESEGUITO", "APPROVATO", PAGATO, "SUCCESSO", "COMPLETATO");
        if (stato != null) {
            return stato;
        }

        return StatoPagamento.values()[0];
    }

    /**
     * Risolve lo stato negativo del pagamento.
     */
    private StatoPagamento statoKo() {
        StatoPagamento stato = pickStato(FALLITO, RIFIUTATO, NEGATO, KO);
        if (stato != null) {
            return stato;
        }

        return StatoPagamento.values()[0];
    }
}
