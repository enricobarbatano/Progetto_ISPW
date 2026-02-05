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
        return new LogicControllerPrenotazioneCampo().trovaSlotDisponibili(input).stream()
            .map(s -> s.getData() + " " + s.getOraInizio() + "-" + s.getOraFine() + " (€" + s.getCosto() + ")")
            .toList();
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

    /**
     * Cerca disponibilità con input grezzo.
     */
    public void cercaDisponibilitaRaw(int idCampo, String data, String oraInizio, int durataMin) {
        ParametriVerificaBean input = new ParametriVerificaBean();
        input.setIdCampo(idCampo);
        input.setData(data);
        input.setOraInizio(oraInizio);
        input.setDurataMin(durataMin);
        cercaDisponibilita(input);
    }

    /**
     * Crea prenotazione con input grezzo.
     */
    public void creaPrenotazioneRaw(int idCampo, String data, String oraInizio, String oraFine,
                                    SessioneUtenteBean sessione) {
        DatiInputPrenotazioneBean input = new DatiInputPrenotazioneBean();
        input.setIdCampo(idCampo);
        input.setData(data);
        input.setOraInizio(oraInizio);
        input.setOraFine(oraFine);
        creaPrenotazione(input, sessione);
    }

    /**
     * Procedi al pagamento con input grezzo.
     */
    public void procediAlPagamentoRaw(String metodo, String credenziale, float importo,
                                      SessioneUtenteBean sessione) {
        DatiPagamentoBean pagamento = new DatiPagamentoBean();
        pagamento.setMetodo(metodo);
        pagamento.setCredenziale(credenziale);
        pagamento.setImporto(importo);
        procediAlPagamento(pagamento, sessione);
    }

}
