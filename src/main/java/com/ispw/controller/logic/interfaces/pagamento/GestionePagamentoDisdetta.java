package com.ispw.controller.logic.interfaces.pagamento;

/**
 * Interfaccia del servizio pagamento usata dal caso d'uso "Disdetta prenotazione".
 *
 * Il controller disdetta non deve accedere direttamente al PagamentoDAO.
 * Per recuperare informazioni sui pagamenti deve passare da questa interfaccia.
 */
public interface GestionePagamentoDisdetta {

    /**
     * Recupera l'importo effettivamente pagato per una prenotazione.
     *
     * Se non esiste un pagamento associato o l'importo non è disponibile,
     * restituisce 0.
     */
    float recuperaImportoPagato(int idPrenotazione);
}