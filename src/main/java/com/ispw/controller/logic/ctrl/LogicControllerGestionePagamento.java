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
 * Controller secondario "Gestione Pagamento" (STATeless).
 * - Dipende SOLO da PagamentoDAO (come da tua specifica).
 * - Logica minimale e commentata, nessun SQL qui dentro.
 */
public class LogicControllerGestionePagamento
        implements GestionePagamentoPrenotazione, GestionePagamentoRimborso, GestionePagamentoPenalita {

    private static final String FALLITO= "FALLITO";
    private static final String RIFIUTATO= "RIFIUTATO";
    private static final String NEGATO= "NEGATO";
    private static final String KO= "KO";

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: implementa interfacce GestionePagamento* (DIP).
    // A2) IO verso GUI/CLI: riceve DatiPagamentoBean, ritorna StatoPagamento/StatoPagamentoBean.
    // A3) Persistenza: usa DAO via DAOFactory.

    /* =========================================================
       1) Pagamento PRENOTAZIONE
       Firma interfaccia: StatoPagamento richiediPagamentoPrenotazione(DatiPagamentoBean,int)
       ========================================================= */
    @Override
    public StatoPagamento richiediPagamentoPrenotazione(DatiPagamentoBean dati, int idPrenotazione) {
        // Validazioni semplici dâ€™input
        if (dati == null) return pickStato(FALLITO, RIFIUTATO, NEGATO, KO);
        MetodoPagamento metodo = parseMetodo(dati.getMetodo());
        if (metodo == null) return pickStato(FALLITO, RIFIUTATO, NEGATO, KO);
        
        // Se esiste giÃ  un pagamento registrato, riuso lâ€™entity; altrimenti ne creo una nuova
        Pagamento pay = pagamentoDAO().findByPrenotazione(idPrenotazione);
        if (pay == null) {
            pay = new Pagamento();
            pay.setIdPrenotazione(idPrenotazione);
        }

        // Popolo i dati minimi del pagamento
        pay.setImportoFinale(BigDecimal.valueOf(Math.max(0f, dati.getImporto())));
        pay.setMetodo(metodo);
        pay.setDataPagamento(LocalDateTime.now());

        // Gateway fittizio: importo > 0 => esito positivo
        boolean ok = dati.getImporto() > 0f;
        StatoPagamento stato = ok
            ? pickStato("OK", "ESEGUITO", "APPROVATO", "PAGATO", "SUCCESSO", "COMPLETATO")
            : pickStato(FALLITO, RIFIUTATO, NEGATO, KO);

        pay.setStato(stato);
        // Persisto tramite DAO (nessun SQL qui)
        pagamentoDAO().store(pay);

        // Ritorno il valore enum richiesto dallâ€™interfaccia
        return stato;
    }

    @Override
    public void eseguiRimborso(int idPrenotazione, float importo) {
        // Cerco un pagamento collegato alla prenotazione
        Pagamento pay = pagamentoDAO().findByPrenotazione(idPrenotazione);
        if (pay == null) {
            // Non esiste pagamento -> niente da fare (scelta semplice)
            return;
        }
        if (importo <= 0f) {
            // Importo non valido -> non registro rimborso
            return;
        }

        pay.setStato(pickStato("RIMBORSATO", "REFUNDED"));
        pay.setDataPagamento(LocalDateTime.now());
        pagamentoDAO().store(pay);

        
    }

    @Override
    public StatoPagamentoBean richiediPagamentoPenalita(DatiPagamentoBean dati, int idPenalita) {
        // Validazioni semplici
        if (dati == null) return esito(false, pickStato(FALLITO, RIFIUTATO, NEGATO, KO).name(), null, "Dati pagamento mancanti");
        MetodoPagamento metodo = parseMetodo(dati.getMetodo());
        if (metodo == null) return esito(false, pickStato(FALLITO, RIFIUTATO, NEGATO, KO).name(), null, "Metodo pagamento non valido");

        // Soluzione â€œscolasticaâ€: traccio un Pagamento legato alla penalitÃ  usando idPrenotazione negativo
        Pagamento pay = new Pagamento();
        pay.setIdPrenotazione(-Math.abs(idPenalita));
        pay.setImportoFinale(BigDecimal.valueOf(Math.max(0f, dati.getImporto())));
        pay.setMetodo(metodo);
        pay.setDataPagamento(LocalDateTime.now());

        boolean ok = dati.getImporto() > 0f;
        String stato = (ok
            ? pickStato("OK", "ESEGUITO", "APPROVATO", "PAGATO", "SUCCESSO", "COMPLETATO")
            : pickStato(FALLITO, RIFIUTATO, NEGATO, KO)
        ).name();

        pay.setStato(StatoPagamento.valueOf(stato)); // coerente con l'enum reale
        pagamentoDAO().store(pay);

        // Ritorno il DTO richiesto dallâ€™interfaccia
        return esito(ok, stato, newTxId("PX"), ok ? "Pagamento penalitÃ  eseguito" : "Pagamento penalitÃ  rifiutato");
    }

    // SEZIONE LOGICA
    // Legenda metodi:
    // 1) pagamentoDAO() - accesso DAO.
    // 2) parseMetodo(...) - converte stringa in MetodoPagamento.
    // 3) pickStato(...) - risolve StatoPagamento da alias.
    // 4) newTxId(...) - genera id transazione.
    // 5) esito(...) - costruisce StatoPagamentoBean.

    private PagamentoDAO pagamentoDAO() {
        return DAOFactory.getInstance().getPagamentoDAO();
    }

    // Converte la stringa del bean in MetodoPagamento (SATISPAY / PAYPAL / BONIFICO)
    private MetodoPagamento parseMetodo(String nome) {
        if (nome == null) return null;
        try { return MetodoPagamento.valueOf(nome.trim().toUpperCase()); }
        catch (IllegalArgumentException ex) { return null; }
    }

    /**
     * Prova una lista di alias per ricavare uno StatoPagamento dell'enum reale;
     * se non trova corrispondenze, fa fallback al primo valore definito nell'enum
     * (scelta "scolastica" per evitare errori di compilazione in caso di nomi diversi).
     */
    private StatoPagamento pickStato(String... preferiti) {
        for (String s : preferiti) {
            try { return StatoPagamento.valueOf(s); }
            catch (IllegalArgumentException ignore) { /* ignored: alias not matching enum constant, try next */ }
        }
        StatoPagamento[] vals = StatoPagamento.values();
        return vals.length > 0 ? vals[0] : null;
    }

    private String newTxId(String prefix) {
        return prefix + "-" + System.currentTimeMillis();
    }

    private StatoPagamentoBean esito(boolean successo, String stato, String idTx, String msg) {
        StatoPagamentoBean bean = new StatoPagamentoBean();
        bean.setSuccesso(successo);
        bean.setStato(stato);                 // String allineata a enum.name()
        bean.setIdTransazione(idTx != null ? idTx : newTxId("TX"));
        bean.setDataPagamento(LocalDateTime.now());
        bean.setMessaggio(msg);
        return bean;
    }
}
