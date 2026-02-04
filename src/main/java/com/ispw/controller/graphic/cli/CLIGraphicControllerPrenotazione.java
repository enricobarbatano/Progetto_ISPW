package com.ispw.controller.graphic.cli;

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
 * Adapter CLI per la prenotazione campo.
 */
public class CLIGraphicControllerPrenotazione implements GraphicControllerPrenotazione {
    
    @SuppressWarnings("java:S1312")
    private Logger log() { return Logger.getLogger(getClass().getName()); }

    private final GraphicControllerNavigation navigator;
    
    public CLIGraphicControllerPrenotazione(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }
    
    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_PRENOTAZIONE;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        // Metodo intenzionalmente vuoto: lifecycle non ancora implementato
    }

    /**
     * Cerca slot disponibili.
     */
    @Override
    public void cercaDisponibilita(ParametriVerificaBean input) {
        if (input == null) {
                    GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_PRENOTAZIONE,
                        GraphicControllerUtils.PREFIX_PRENOT,
                    "Parametri ricerca disponibilità nulli");
            return;
        }

        try {
            LogicControllerPrenotazioneCampo logicController = new LogicControllerPrenotazioneCampo();
            List<String> slot = logicController.trovaSlotDisponibili(input).stream()
                .map(s -> s.getData() + " " + s.getOraInizio() + "-" + s.getOraFine() + " (€" + s.getCosto() + ")")
                .toList();

            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_PRENOTAZIONE, Map.of("slotDisponibili", slot));
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore ricerca disponibilità", e);
        }
    }

    @Override
    public void creaPrenotazione(DatiInputPrenotazioneBean input, SessioneUtenteBean sessione) {
        if (input == null) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_PRENOTAZIONE,
                    GraphicControllerUtils.PREFIX_PRENOT, "Dati prenotazione nulli");
            return;
        }
        if (sessione == null) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_PRENOTAZIONE,
                    GraphicControllerUtils.PREFIX_PRENOT,
                    "Sessione utente mancante per prenotazione");
            return;
        }

        try {
            LogicControllerPrenotazioneCampo logicController = new LogicControllerPrenotazioneCampo();
            RiepilogoPrenotazioneBean riepilogo = logicController.nuovaPrenotazione(input, sessione);

            if (riepilogo == null) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_PRENOTAZIONE,
                    GraphicControllerUtils.PREFIX_PRENOT,
                    "Prenotazione non creata");
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
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_PRENOTAZIONE,
                    GraphicControllerUtils.PREFIX_PRENOT, "Dati pagamento nulli");
            return;
        }
        if (sessione == null) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_PRENOTAZIONE,
                    GraphicControllerUtils.PREFIX_PRENOT,
                    "Sessione utente mancante per pagamento");
            return;
        }

        try {
            LogicControllerPrenotazioneCampo logicController = new LogicControllerPrenotazioneCampo();
            StatoPagamentoBean esito = logicController.completaPrenotazione(pagamento, sessione);

            if (esito == null) {
                GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_PRENOTAZIONE,
                    GraphicControllerUtils.PREFIX_PRENOT,
                    "Pagamento non completato");
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
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_HOME);
        }
    }

}
