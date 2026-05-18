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

/**
 * Controller grafico astratto del caso d'uso "Prenota campo".
 *
 * Questa classe contiene la logica comune tra GUI e CLI:
 * - valida gli input ricevuti dalla view;
 * - chiama il controller logico tramite interfaccia;
 * - formatta i dati da mostrare;
 * - usa il navigator per cambiare schermata o aggiornare la stessa route.
 *
 * Nota di progetto:
 * il graphic controller non conosce l'implementazione concreta del logic controller.
 * Usa CtrlPrenotazione ottenuto tramite LogicControllerFactory.
 */
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
        // In questa schermata non è necessario gestire parametri in modo comune.
    }

    @Override
    public void cercaDisponibilita(ParametriVerificaBean input) {
        if (input == null) {
            notifyPrenotazioneError(GraphicControllerUtils.MSG_PARAMETRI_RICERCA_DISPONIBILITA_NULLI);
            return;
        }

        try {
            List<String> slot = GraphicControllerPrenotazioneUtils.formatSlotDisponibili(
                    logicController().trovaSlotDisponibili(input));

            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_PRENOTAZIONE,
                        Map.of(GraphicControllerUtils.KEY_SLOT_DISPONIBILI, slot));
            }
        } catch (RuntimeException ex) {
            log().log(Level.SEVERE, "Errore ricerca disponibilita", ex);
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
            RiepilogoPrenotazioneBean riepilogo = logicController().nuovaPrenotazione(input, sessione);

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
        } catch (RuntimeException ex) {
            log().log(Level.SEVERE, "Errore creazione prenotazione", ex);
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
            StatoPagamentoBean esito = logicController().completaPrenotazione(pagamento, sessione);

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
        } catch (RuntimeException ex) {
            log().log(Level.SEVERE, "Errore pagamento", ex);
        }
    }

    @Override
    public void tornaAllaHome() {
        goToHome();
    }

    public void richiediListaCampi(SessioneUtenteBean sessione) {
        try {
            List<String> campi = formatCampi(logicController().listaCampi());

            if (navigator != null) {
                Map<String, Object> payload = new HashMap<>();
                payload.put(GraphicControllerUtils.KEY_CAMPI, campi);

                if (sessione != null) {
                    payload.put(GraphicControllerUtils.KEY_SESSIONE, sessione);
                }

                navigator.goTo(GraphicControllerUtils.ROUTE_PRENOTAZIONE, payload);
            }
        } catch (RuntimeException ex) {
            log().log(Level.SEVERE, "Errore recupero lista campi", ex);
        }
    }

    private List<String> formatCampi(CampiBean campi) {
        if (campi == null || campi.getCampi() == null) {
            return List.of();
        }

        return campi.getCampi().stream()
                .map(c -> String.format("#%d - %s (%s) [attivo=%s, manutenzione=%s]",
                        c.getIdCampo(),
                        c.getNome(),
                        c.getTipoSport(),
                        c.isAttivo(),
                        c.isFlagManutenzione()))
                .toList();
    }

    public void cercaDisponibilitaRaw(int idCampo, String data, String oraInizio, int durataMin) {
        ParametriVerificaBean input = new ParametriVerificaBean();
        input.setIdCampo(idCampo);
        input.setData(data);
        input.setOraInizio(oraInizio);
        input.setDurataMin(durataMin);

        cercaDisponibilita(input);
    }

    public void creaPrenotazioneRaw(int idCampo, String data, String oraInizio, String oraFine,
                                    SessioneUtenteBean sessione) {
        DatiInputPrenotazioneBean input = new DatiInputPrenotazioneBean();
        input.setIdCampo(idCampo);
        input.setData(data);
        input.setOraInizio(oraInizio);
        input.setOraFine(oraFine);

        creaPrenotazione(input, sessione);
    }

    public void procediAlPagamentoRaw(String metodo, String credenziale, float importo,
                                      SessioneUtenteBean sessione) {
        DatiPagamentoBean pagamento = new DatiPagamentoBean();
        pagamento.setMetodo(metodo);
        pagamento.setCredenziale(credenziale);
        pagamento.setImporto(importo);

        procediAlPagamento(pagamento, sessione);
    }
}
