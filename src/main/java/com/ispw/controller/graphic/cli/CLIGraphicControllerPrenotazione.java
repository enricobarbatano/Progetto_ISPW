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
        return "prenotazione";
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
                GraphicControllerUtils.notifyError(log(), navigator, "prenotazione", "[PRENOT]",
                    "Parametri ricerca disponibilità nulli");
            return;
        }

        try {
            LogicControllerPrenotazioneCampo logicController = new LogicControllerPrenotazioneCampo();
            List<String> slot = logicController.trovaSlotDisponibili(input).stream()
                .map(s -> s.getData() + " " + s.getOraInizio() + "-" + s.getOraFine() + " (€" + s.getCosto() + ")")
                .toList();

            if (navigator != null) {
                navigator.goTo("prenotazione", Map.of("slotDisponibili", slot));
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore ricerca disponibilità", e);
        }
    }

    @Override
    public void creaPrenotazione(DatiInputPrenotazioneBean input, SessioneUtenteBean sessione) {
        if (input == null) {
            GraphicControllerUtils.notifyError(log(), navigator, "prenotazione", "[PRENOT]", "Dati prenotazione nulli");
            return;
        }
        if (sessione == null) {
            GraphicControllerUtils.notifyError(log(), navigator, "prenotazione", "[PRENOT]",
                    "Sessione utente mancante per prenotazione");
            return;
        }

        try {
            LogicControllerPrenotazioneCampo logicController = new LogicControllerPrenotazioneCampo();
            RiepilogoPrenotazioneBean riepilogo = logicController.nuovaPrenotazione(input, sessione);

            if (riepilogo == null) {
                GraphicControllerUtils.notifyError(log(), navigator, "prenotazione", "[PRENOT]",
                    "Prenotazione non creata");
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("idPrenotazione", riepilogo.getIdPrenotazione());
            payload.put("importoTotale", riepilogo.getImportoTotale());
            payload.put("riepilogo", riepilogo.toString());

            if (navigator != null) {
                navigator.goTo("prenotazione", Map.of("riepilogo", payload));
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore creazione prenotazione", e);
        }
    }

    @Override
    public void procediAlPagamento(DatiPagamentoBean pagamento, SessioneUtenteBean sessione) {
        if (pagamento == null) {
            GraphicControllerUtils.notifyError(log(), navigator, "prenotazione", "[PRENOT]", "Dati pagamento nulli");
            return;
        }
        if (sessione == null) {
            GraphicControllerUtils.notifyError(log(), navigator, "prenotazione", "[PRENOT]",
                    "Sessione utente mancante per pagamento");
            return;
        }

        try {
            LogicControllerPrenotazioneCampo logicController = new LogicControllerPrenotazioneCampo();
            StatoPagamentoBean esito = logicController.completaPrenotazione(pagamento, sessione);

            if (esito == null) {
                GraphicControllerUtils.notifyError(log(), navigator, "prenotazione", "[PRENOT]",
                    "Pagamento non completato");
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("successo", esito.isSuccesso());
            payload.put("stato", esito.getStato());
            payload.put("messaggio", esito.getMessaggio());
            payload.put("idTransazione", esito.getIdTransazione());
            payload.put("dataPagamento", esito.getDataPagamento());

            if (navigator != null) {
                navigator.goTo("prenotazione", Map.of("pagamento", payload));
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore pagamento", e);
        }
    }

    @Override
    public void tornaAllaHome() {
        if (navigator != null) {
            navigator.goTo("home");
        }
    }

}
