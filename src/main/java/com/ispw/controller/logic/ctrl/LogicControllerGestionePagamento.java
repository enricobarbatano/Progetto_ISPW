package com.ispw.controller.logic.ctrl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ispw.bean.DatiPagamentoBean; // GestionePagamentoPrenotazione, GestionePagamentoRimborso, GestionePagamentoPenalita
import com.ispw.bean.StatoPagamentoBean;      // factory per ottenere il DAO in modo stateless
import com.ispw.controller.logic.interfaces.pagamento.GestionePagamentoPenalita; // <- unica dipendenza
import com.ispw.controller.logic.interfaces.pagamento.GestionePagamentoPrenotazione;
import com.ispw.controller.logic.interfaces.pagamento.GestionePagamentoRimborso;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.PagamentoDAO;
import com.ispw.model.entity.Pagamento;
import com.ispw.model.enums.MetodoPagamento;
import com.ispw.model.enums.StatoPagamento;

/**
 * Controller secondario "Gestione Pagamento" (STATELESS).
 * - Persistenza via DAOFactory (runtime).
 * - Nessun SQL qui.
 */
public class LogicControllerGestionePagamento
        implements GestionePagamentoPrenotazione, GestionePagamentoRimborso, GestionePagamentoPenalita {

    private static final String FALLITO   = "FALLITO";
    private static final String RIFIUTATO = "RIFIUTATO";
    private static final String NEGATO    = "NEGATO";
    private static final String KO        = "KO";

    /* =========================================================
       1) Pagamento PRENOTAZIONE
       Firma: StatoPagamento richiediPagamentoPrenotazione(DatiPagamentoBean,int)
       ========================================================= */
    @Override
    public StatoPagamento richiediPagamentoPrenotazione(DatiPagamentoBean dati, int idPrenotazione) {

        if (dati == null) return statoKo();

        MetodoPagamento metodo = parseMetodo(dati.getMetodo());
        if (metodo == null) return statoKo();

        // Recupera eventuale pagamento già presente (ultimo pagamento per prenotazione)
        Pagamento pay = pagamentoDAO().findByPrenotazione(idPrenotazione);
        if (pay == null) {
            pay = new Pagamento();
            pay.setIdPrenotazione(idPrenotazione);
        }

        // Popola dati minimi
        float imp = Math.max(0f, dati.getImporto());
        pay.setImportoFinale(BigDecimal.valueOf(imp));
        pay.setMetodo(metodo);
        pay.setDataPagamento(LocalDateTime.now());

        // Gateway fittizio: importo > 0 => ok
        boolean ok = imp > 0f;
        StatoPagamento stato = ok ? statoOk() : statoKo();

        pay.setStato(stato);
        pagamentoDAO().store(pay);

        return stato;
    }

    /* =========================================================
       2) Rimborso PRENOTAZIONE
       Firma: void eseguiRimborso(int,float)
       ========================================================= */
    @Override
    public void eseguiRimborso(int idPrenotazione, float importo) {
        Pagamento pay = pagamentoDAO().findByPrenotazione(idPrenotazione);
        if (pay == null) return;
        if (importo <= 0f) return;

        // Stato rimborso: prova alias comuni, altrimenti fallback
        StatoPagamento statoRimborso = pickStato("RIMBORSATO", "REFUNDED");

        pay.setStato(statoRimborso);
        pay.setDataPagamento(LocalDateTime.now());
        pagamentoDAO().store(pay);
    }

    /* =========================================================
       3) Pagamento PENALITA'
       Firma: StatoPagamentoBean richiediPagamentoPenalita(DatiPagamentoBean,int)
       ========================================================= */
    @Override
    public StatoPagamentoBean richiediPagamentoPenalita(DatiPagamentoBean dati, int idPenalita) {

        if (dati == null) {
            return esito(false, statoKo().name(), null, "Dati pagamento mancanti");
        }

        MetodoPagamento metodo = parseMetodo(dati.getMetodo());
        if (metodo == null) {
            return esito(false, statoKo().name(), null, "Metodo pagamento non valido");
        }

        // Soluzione “scolastica”: pagamento legato alla penalità usando idPrenotazione negativo
        Pagamento pay = new Pagamento();
        pay.setIdPrenotazione(-Math.abs(idPenalita));

        float imp = Math.max(0f, dati.getImporto());
        pay.setImportoFinale(BigDecimal.valueOf(imp));
        pay.setMetodo(metodo);
        pay.setDataPagamento(LocalDateTime.now());

        boolean ok = imp > 0f;
        StatoPagamento stato = ok ? statoOk() : statoKo();

        pay.setStato(stato);
        pagamentoDAO().store(pay);

        return esito(ok, stato.name(), newTxId("PX"),
                ok ? "Pagamento penalita eseguito" : "Pagamento penalita rifiutato");
    }

    /* ===================== HELPERS ===================== */

    private PagamentoDAO pagamentoDAO() {
        return DAOFactory.getInstance().getPagamentoDAO();
    }

    private MetodoPagamento parseMetodo(String nome) {
        if (nome == null) return null;
        try { return MetodoPagamento.valueOf(nome.trim().toUpperCase()); }
        catch (IllegalArgumentException ex) { return null; }
    }

    /**
     * Prova alias per ricavare uno StatoPagamento reale; se nessuno matcha,
     * fallback al primo valore dell'enum (scelta “scolastica” per robustezza).
     */
    private StatoPagamento pickStato(String... preferiti) {
        for (String s : preferiti) {
            try { return StatoPagamento.valueOf(s); }
            catch (IllegalArgumentException ignore) { /* try next */ }
        }
        StatoPagamento[] vals = StatoPagamento.values();
        return vals.length > 0 ? vals[0] : null;
    }

    private StatoPagamento statoOk() {
        // prova alias più comuni: in base al tuo enum reale ne matcha almeno uno
        StatoPagamento s = pickStato("OK", "ESEGUITO", "APPROVATO", "PAGATO", "SUCCESSO", "COMPLETATO");
        // in caso limite pickStato può tornare null se enum vuoto: fallback hard
        return s != null ? s : StatoPagamento.values()[0];
    }

    private StatoPagamento statoKo() {
        StatoPagamento s = pickStato(FALLITO, RIFIUTATO, NEGATO, KO);
        return s != null ? s : StatoPagamento.values()[0];
    }

    private String newTxId(String prefix) {
        return prefix + "-" + System.currentTimeMillis();
    }

    private StatoPagamentoBean esito(boolean successo, String stato, String idTx, String msg) {
        StatoPagamentoBean bean = new StatoPagamentoBean();
        bean.setSuccesso(successo);
        bean.setStato(stato);
        bean.setIdTransazione(idTx != null ? idTx : newTxId("TX"));
        bean.setDataPagamento(LocalDateTime.now());
        bean.setMessaggio(msg);
        return bean;
    }
}
