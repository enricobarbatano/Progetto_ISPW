package com.ispw.controller.graphic.gui;

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
 * Adapter GUI per la prenotazione campo.
 * Trasforma dati grezzi (String, int) in bean per il LogicController.
 */
public class GUIGraphicControllerPrenotazione implements GraphicControllerPrenotazione {
    
    @SuppressWarnings("java:S1312")
    private Logger log() { return Logger.getLogger(getClass().getName()); }
    
    private final GraphicControllerNavigation navigator;
    
    public GUIGraphicControllerPrenotazione(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }
    
    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_PRENOTAZIONE;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        // Lifecycle hook: View inizializza form ricerca
    }

    @Override
    public void cercaDisponibilita(ParametriVerificaBean input) {
        if (input == null) {
            log().warning("Parametri ricerca disponibilità nulli");
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
            log().warning("Dati prenotazione nulli");
            return;
        }
        if (sessione == null) {
            log().warning("Sessione utente mancante per prenotazione");
            return;
        }
        try {
            LogicControllerPrenotazioneCampo logicController = new LogicControllerPrenotazioneCampo();
            RiepilogoPrenotazioneBean riepilogo = logicController.nuovaPrenotazione(input, sessione);

            if (riepilogo == null) {
                log().warning("Prenotazione non creata");
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
            log().warning("Dati pagamento nulli");
            return;
        }
        if (sessione == null) {
            log().warning("Sessione utente mancante per pagamento");
            return;
        }
        try {
            LogicControllerPrenotazioneCampo logicController = new LogicControllerPrenotazioneCampo();
            StatoPagamentoBean esito = logicController.completaPrenotazione(pagamento, sessione);

            if (esito == null) {
                log().warning("Pagamento non completato");
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
            navigator.goTo(GraphicControllerUtils.ROUTE_HOME, null);
        }
    }
    
}
