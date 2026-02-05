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

    protected abstract List<String> trovaSlotDisponibili(ParametriVerificaBean input);

    protected abstract RiepilogoPrenotazioneBean nuovaPrenotazione(DatiInputPrenotazioneBean input,
                                                                   SessioneUtenteBean sessione);

    protected abstract StatoPagamentoBean completaPrenotazione(DatiPagamentoBean pagamento,
                                                               SessioneUtenteBean sessione);

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
            List<String> slot = trovaSlotDisponibili(input);

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
            RiepilogoPrenotazioneBean riepilogo = nuovaPrenotazione(input, sessione);

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
            StatoPagamentoBean esito = completaPrenotazione(pagamento, sessione);

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
