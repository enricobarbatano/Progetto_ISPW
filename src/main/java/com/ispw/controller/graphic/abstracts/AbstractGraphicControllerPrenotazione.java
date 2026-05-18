package com.ispw.controller.graphic.abstracts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.CampiBean;
import com.ispw.bean.DatiInputPrenotazioneBean;
import com.ispw.bean.DatiPagamentoBean;
import com.ispw.bean.ParametriVerificaBean;
import com.ispw.bean.RiepilogoPrenotazioneBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.bean.StatoPagamentoBean;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerPrenotazione;
import com.ispw.controller.graphic.interfaces.GraphicControllerPrenotazioneUtils;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.logic.LogicControllerFactory;
import com.ispw.controller.logic.interfaces.CtrlPrenotazione;

public abstract class AbstractGraphicControllerPrenotazione implements GraphicControllerPrenotazione {

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicControllerPrenotazione(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    protected abstract Logger log();

    protected abstract void notifyPrenotazioneError(String message);

    protected abstract void goToHome();

    protected CtrlPrenotazione logicController() {
        return LogicControllerFactory.getPrenotazioneController();
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_PRENOTAZIONE;
    }

    @Override
    public void onShow(Map<String, Object> params) {
    }

    @Override
public void richiediListaCampi() {

    CampiBean campi = logicController().listaCampi();

    List<String> lista = campi.getCampi().stream()
            .map(c -> c.getIdCampo() + " - " + c.getNome())
            .toList();

    navigator.goTo(
            GraphicControllerUtils.ROUTE_PRENOTAZIONE,
            Map.of(GraphicControllerUtils.KEY_CAMPI, lista)
    );
}
    @Override
    public void cercaDisponibilita(int idCampo, String data, String oraInizio, int durataMin) {

        if (idCampo <= 0 || data == null || oraInizio == null || durataMin <= 0) {
            notifyPrenotazioneError(GraphicControllerUtils.MSG_PARAMETRI_RICERCA_DISPONIBILITA_NULLI);
            return;
        }

        ParametriVerificaBean input = new ParametriVerificaBean();
        input.setIdCampo(idCampo);
        input.setData(data);
        input.setOraInizio(oraInizio);
        input.setDurataMin(durataMin);

        try {
            List<String> slot = GraphicControllerPrenotazioneUtils.formatSlotDisponibili(
                    logicController().trovaSlotDisponibili(input));

            navigator.goTo(GraphicControllerUtils.ROUTE_PRENOTAZIONE,
                    Map.of(GraphicControllerUtils.KEY_SLOT_DISPONIBILI, slot));

        } catch (RuntimeException ex) {
            log().log(Level.SEVERE, "Errore disponibilita", ex);
        }
    }

    @Override
    public void creaPrenotazione(int idCampo,
                                 String data,
                                 String oraInizio,
                                 String oraFine,
                                 SessioneUtenteBean sessione) {

        if (idCampo <= 0 || data == null || oraInizio == null || oraFine == null) {
            notifyPrenotazioneError(GraphicControllerUtils.MSG_DATI_PRENOTAZIONE_NULLI);
            return;
        }

        if (sessione == null) {
            notifyPrenotazioneError(GraphicControllerUtils.MSG_SESSIONE_MANCANTE_PRENOTAZIONE);
            return;
        }

        DatiInputPrenotazioneBean input = new DatiInputPrenotazioneBean();
        input.setIdCampo(idCampo);
        input.setData(data);
        input.setOraInizio(oraInizio);
        input.setOraFine(oraFine);

        try {
            RiepilogoPrenotazioneBean riepilogo =
                    logicController().nuovaPrenotazione(input, sessione);

            if (riepilogo == null) {
                notifyPrenotazioneError(GraphicControllerUtils.MSG_PRENOTAZIONE_NON_CREATA);
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put(GraphicControllerUtils.KEY_ID_PRENOTAZIONE, riepilogo.getIdPrenotazione());
            payload.put(GraphicControllerUtils.KEY_IMPORTO_TOTALE, riepilogo.getImportoTotale());
            payload.put(GraphicControllerUtils.KEY_RIEPILOGO, riepilogo.toString());

            navigator.goTo(GraphicControllerUtils.ROUTE_PRENOTAZIONE,
                    Map.of(GraphicControllerUtils.KEY_RIEPILOGO, payload));

        } catch (RuntimeException ex) {
            log().log(Level.SEVERE, "Errore prenotazione", ex);
        }
    }

    @Override
    public void procediAlPagamento(String metodo,
                                    String credenziale,
                                    float importo,
                                    SessioneUtenteBean sessione) {

        if (metodo == null || credenziale == null || importo <= 0) {
            notifyPrenotazioneError(GraphicControllerUtils.MSG_DATI_PAGAMENTO_NULLI);
            return;
        }

        if (sessione == null) {
            notifyPrenotazioneError(GraphicControllerUtils.MSG_SESSIONE_MANCANTE_PAGAMENTO);
            return;
        }

        DatiPagamentoBean pagamento = new DatiPagamentoBean();
        pagamento.setMetodo(metodo);
        pagamento.setCredenziale(credenziale);
        pagamento.setImporto(importo);

        try {
            StatoPagamentoBean esito =
                    logicController().completaPrenotazione(pagamento, sessione);

            if (esito == null) {
                notifyPrenotazioneError(GraphicControllerUtils.MSG_PAGAMENTO_NON_COMPLETATO);
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put(GraphicControllerUtils.KEY_SUCCESSO, esito.isSuccesso());
            payload.put(GraphicControllerUtils.KEY_STATO, esito.getStato());
            payload.put(GraphicControllerUtils.KEY_MESSAGGIO, esito.getMessaggio());

            navigator.goTo(GraphicControllerUtils.ROUTE_PRENOTAZIONE,
                    Map.of(GraphicControllerUtils.KEY_PAGAMENTO, payload));

        } catch (RuntimeException ex) {
            log().log(Level.SEVERE, "Errore pagamento", ex);
        }
    }

    @Override
    public void tornaAllaHome() {
        goToHome();
    }
}
