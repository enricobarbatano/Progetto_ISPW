package com.ispw.controller.graphic.abstracts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.DatiInputPrenotazioneBean;
import com.ispw.bean.DatiPagamentoBean;
import com.ispw.bean.ParametriVerificaBean;
import com.ispw.bean.RiepilogoPrenotazioneBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.bean.StatoPagamentoBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerPrenotazione;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.logic.ctrl.LogicControllerPrenotazioneCampo;

/**
 * Classe astratta che centralizza la logica comune dei controller grafici Prenotazione
 * (CLI/GUI) per ridurre duplicazione. Non introduce nuove responsabilità né
 * modifica il disaccoppiamento: delega invariata ai LogicController e mantiene
 * la stessa navigazione verso la View tramite GraphicControllerNavigation.
 */
public abstract class AbstractGraphicControllerPrenotazione implements GraphicControllerPrenotazione {

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicControllerPrenotazione(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    protected abstract Logger log();

    protected abstract void notifyPrenotazioneError(String message);

    protected abstract void goToHome();

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_PRENOTAZIONE;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        // lifecycle hook (override if needed)
    }

    @Override
    public void cercaDisponibilita(ParametriVerificaBean input) {
        if (input == null) {
            notifyPrenotazioneError(GraphicControllerUtils.MSG_PARAMETRI_RICERCA_DISPONIBILITA_NULLI);
            return;
        }

        try {
            LogicControllerPrenotazioneCampo logicController = new LogicControllerPrenotazioneCampo();
            List<String> slot = logicController.trovaSlotDisponibili(input).stream()
                .map(s -> s.getData() + " " + s.getOraInizio() + "-" + s.getOraFine() + " (€" + s.getCosto() + ")")
                .toList();

            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_PRENOTAZIONE,
                    Map.of(GraphicControllerUtils.KEY_SLOT_DISPONIBILI, slot));
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore ricerca disponibilità", e);
        }
    }

    @Override
    public void creaPrenotazione(DatiInputPrenotazioneBean input, SessioneUtenteBean sessione) {
        if (input == null) {
            notifyPrenotazioneError(GraphicControllerUtils.MSG_DATI_PRENOTAZIONE_NULLI);
            return;
        }
        if (sessione == null) {
            notifyPrenotazioneError(GraphicControllerUtils.MSG_SESSIONE_MANCANTE_PRENOTAZIONE);
            return;
        }

        try {
            LogicControllerPrenotazioneCampo logicController = new LogicControllerPrenotazioneCampo();
            RiepilogoPrenotazioneBean riepilogo = logicController.nuovaPrenotazione(input, sessione);

            if (riepilogo == null) {
                notifyPrenotazioneError(GraphicControllerUtils.MSG_PRENOTAZIONE_NON_CREATA);
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put(GraphicControllerUtils.KEY_ID_PRENOTAZIONE, riepilogo.getIdPrenotazione());
            payload.put(GraphicControllerUtils.KEY_IMPORTO_TOTALE, riepilogo.getImportoTotale());
            payload.put(GraphicControllerUtils.KEY_RIEPILOGO, riepilogo.toString());

            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_PRENOTAZIONE,
                    Map.of(GraphicControllerUtils.KEY_RIEPILOGO, payload));
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore creazione prenotazione", e);
        }
    }

    @Override
    public void procediAlPagamento(DatiPagamentoBean pagamento, SessioneUtenteBean sessione) {
        if (pagamento == null) {
            notifyPrenotazioneError(GraphicControllerUtils.MSG_DATI_PAGAMENTO_NULLI);
            return;
        }
        if (sessione == null) {
            notifyPrenotazioneError(GraphicControllerUtils.MSG_SESSIONE_MANCANTE_PAGAMENTO);
            return;
        }

        try {
            LogicControllerPrenotazioneCampo logicController = new LogicControllerPrenotazioneCampo();
            StatoPagamentoBean esito = logicController.completaPrenotazione(pagamento, sessione);

            if (esito == null) {
                notifyPrenotazioneError(GraphicControllerUtils.MSG_PAGAMENTO_NON_COMPLETATO);
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put(GraphicControllerUtils.KEY_SUCCESSO, esito.isSuccesso());
            payload.put(GraphicControllerUtils.KEY_STATO, esito.getStato());
            payload.put(GraphicControllerUtils.KEY_MESSAGGIO, esito.getMessaggio());
            payload.put(GraphicControllerUtils.KEY_ID_TRANSAZIONE, esito.getIdTransazione());
            payload.put(GraphicControllerUtils.KEY_DATA_PAGAMENTO, esito.getDataPagamento());

            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_PRENOTAZIONE,
                    Map.of(GraphicControllerUtils.KEY_PAGAMENTO, payload));
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore pagamento", e);
        }
    }

    @Override
    public void tornaAllaHome() {
        goToHome();
    }
}
