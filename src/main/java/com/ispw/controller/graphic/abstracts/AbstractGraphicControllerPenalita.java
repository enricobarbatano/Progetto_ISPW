package com.ispw.controller.graphic.abstracts;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.DatiPenalitaBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.UtenteSelezioneBean;
import com.ispw.bean.UtentiBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerPenalita;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.logic.ctrl.LogicControllerApplicaPenalita;

public abstract class AbstractGraphicControllerPenalita implements GraphicControllerPenalita {

    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: implementa GraphicControllerPenalita (interfaccia) e usa GraphicControllerNavigation.
    // A2) IO verso GUI/CLI: riceve/ritorna bean (DatiPenalitaBean, UtentiBean) e Map.
    // A3) Logica delegata: usa LogicControllerApplicaPenalita.

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicControllerPenalita(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    protected abstract Logger log();

    protected abstract void goToHome();

    protected LogicControllerApplicaPenalita logicController() {
        return new LogicControllerApplicaPenalita();
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_PENALITA;
    }

    @Override
    public void onShow(Map<String, Object> params) {
    }

    public void richiediListaUtenti() {
        try {
            List<String> utenti = formatUtenti(logicController().listaUtentiPerPenalita());
            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_PENALITA,
                    Map.of(GraphicControllerUtils.KEY_UTENTI, utenti));
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore recupero lista utenti", e);
        }
    }

    @Override
    public void selezionaUtente(String email) {
        if (isEmailNonValida(email)) {
            return;
        }
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_PENALITA,
                Map.of(GraphicControllerUtils.KEY_EMAIL, email.trim()));
        }
    }

    @Override
    public void applicaPenalita(int idUtente, float importo, String motivazione) {
        if (isIdUtenteNonValido(idUtente) || isPenalitaNonValida(importo, motivazione)) {
            return;
        }

        try {
            DatiPenalitaBean dati = buildPenalitaBean(idUtente, importo, motivazione);

            EsitoOperazioneBean esito = logicController().applicaSanzione(
                dati,
                null,
                null,
                null,
                null,
                null);

            if (esito == null || !esito.isSuccesso()) {
                notifyPenalitaError(esito != null ? esito.getMessaggio()
                    : GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
                return;
            }

            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_PENALITA,
                    Map.of(GraphicControllerUtils.KEY_SUCCESSO, esito.getMessaggio()));
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore applicazione penalitÃ ", e);
        }
    }

    @Override
    public void tornaAllaHome() {
        goToHome();
    }

    // SEZIONE LOGICA
    // Legenda metodi:
    // 1) notifyPenalitaError(...) - notifica errore e naviga.
    // 2) isEmailNonValida(...) - valida email.
    // 3) isIdUtenteNonValido(...) - valida id utente.
    // 4) isPenalitaNonValida(...) - valida penalita.
    // 5) buildPenalitaBean(...) - costruisce bean penalita.
    // 6) formatUtenti(...) - formatta lista utenti.
    // 7) formatUtente(...) - formatta singolo utente.
    private void notifyPenalitaError(String message) {
        GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_PENALITA,
            GraphicControllerUtils.PREFIX_PENALITA, message);
    }

    private boolean isEmailNonValida(String email) {
        if (email == null || email.isBlank()) {
            notifyPenalitaError(GraphicControllerUtils.MSG_EMAIL_UTENTE_NON_VALIDA);
            return true;
        }
        return false;
    }

    private boolean isIdUtenteNonValido(int idUtente) {
        if (idUtente <= 0) {
            notifyPenalitaError(GraphicControllerUtils.MSG_ID_UTENTE_NON_VALIDO);
            return true;
        }
        return false;
    }

    private boolean isPenalitaNonValida(float importo, String motivazione) {
        if (motivazione == null || motivazione.isBlank() || importo <= 0) {
            notifyPenalitaError(GraphicControllerUtils.MSG_DATI_PENALITA_NON_VALIDI);
            return true;
        }
        return false;
    }

    private DatiPenalitaBean buildPenalitaBean(int idUtente, float importo, String motivazione) {
        DatiPenalitaBean dati = new DatiPenalitaBean();
        dati.setIdUtente(idUtente);
        dati.setMotivazione(motivazione.trim());
        dati.setImporto(BigDecimal.valueOf(importo));
        return dati;
    }

    private List<String> formatUtenti(UtentiBean utenti) {
        if (utenti == null || utenti.getUtenti() == null || utenti.getUtenti().isEmpty()) {
            return List.of("Nessun utente disponibile");
        }
        return utenti.getUtenti().stream()
            .map(this::formatUtente)
            .toList();
    }

    private String formatUtente(UtenteSelezioneBean u) {
        if (u == null) {
            return "";
        }
        String email = u.getEmail() != null ? u.getEmail() : "";
        return String.format("#%d - %s (%s)", u.getIdUtente(), email, u.getRuolo());
    }
}
