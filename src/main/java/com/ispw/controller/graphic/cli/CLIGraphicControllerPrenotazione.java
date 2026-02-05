package com.ispw.controller.graphic.cli;

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
import com.ispw.controller.graphic.GraphicControllerPrenotazioneUtils;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerPrenotazione;
import com.ispw.controller.logic.ctrl.LogicControllerPrenotazioneCampo;

/**
 * Adapter CLI per la prenotazione campo.
 */
public class CLIGraphicControllerPrenotazione extends AbstractGraphicControllerPrenotazione {
    
    public CLIGraphicControllerPrenotazione(GraphicControllerNavigation navigator) {
        super(navigator);
    }
    @SuppressWarnings("java:S1312")
    @Override
    protected Logger log() { return Logger.getLogger(getClass().getName()); }

    @Override
    protected void notifyPrenotazioneError(String message) {
        GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_PRENOTAZIONE,
            GraphicControllerUtils.PREFIX_PRENOT, message);
    }

    @Override
    protected void goToHome() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_HOME);
        }
    }

    @Override
    protected List<String> trovaSlotDisponibili(ParametriVerificaBean input) {
        return GraphicControllerPrenotazioneUtils.formatSlotDisponibili(
            new LogicControllerPrenotazioneCampo().trovaSlotDisponibili(input));
    }

    @Override
    protected RiepilogoPrenotazioneBean nuovaPrenotazione(DatiInputPrenotazioneBean input,
                                                          SessioneUtenteBean sessione) {
        return new LogicControllerPrenotazioneCampo().nuovaPrenotazione(input, sessione);
    }

    @Override
    protected StatoPagamentoBean completaPrenotazione(DatiPagamentoBean pagamento, SessioneUtenteBean sessione) {
        return new LogicControllerPrenotazioneCampo().completaPrenotazione(pagamento, sessione);
    }

    /**
     * Recupera la lista campi per la selezione UI (formato testuale).
     */
    public void richiediListaCampi(SessioneUtenteBean sessione) {
        try {
            List<String> campi = new LogicControllerPrenotazioneCampo().listaCampi();
            if (navigator != null) {
                Map<String, Object> payload = new java.util.HashMap<>();
                payload.put(GraphicControllerUtils.KEY_CAMPI, campi);
                if (sessione != null) {
                    payload.put(GraphicControllerUtils.KEY_SESSIONE, sessione);
                }
                navigator.goTo(GraphicControllerUtils.ROUTE_PRENOTAZIONE, payload);
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore recupero lista campi", e);
        }
    }

}
