package com.ispw.controller.graphic.abstracts;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.RichiestaDisdettaBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerRichiesteDisdetta;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.logic.ctrl.LogicControllerDisdettaPrenotazione;

public abstract class AbstractGraphicControllerRichiesteDisdetta implements GraphicControllerRichiesteDisdetta {

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicControllerRichiesteDisdetta(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    protected abstract Logger log();
    protected abstract void goToHome();

    protected LogicControllerDisdettaPrenotazione logicController() {
        return new LogicControllerDisdettaPrenotazione();
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_RICHIESTE_DISDETTA;
    }

    @Override
    public void onShow(Map<String, Object> params) {
        GraphicControllerUtils.handleOnShow(log(), params, GraphicControllerUtils.PREFIX_RICHIESTE_DISDETTA);
    }

    @Override
    public void caricaRichiestePending(SessioneUtenteBean sessioneGestore) {
        if (isSessioneNonValida(sessioneGestore, GraphicControllerUtils.MSG_SESSIONE_NON_VALIDA)) {
            return;
        }

        try {
            List<RichiestaDisdettaBean> pending =
                    logicController().listaRichiestePending(sessioneGestore);

            List<String> elenco = pending.stream()
                    .map(Object::toString)
                    .toList();

            if (navigator != null) {
                navigator.goTo(
                        GraphicControllerUtils.ROUTE_RICHIESTE_DISDETTA,
                        Map.of(GraphicControllerUtils.KEY_RICHIESTE, elenco)
                );
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore caricamento richieste disdetta", e);
            notifyError("Errore caricamento richieste disdetta");
        }
    }

    @Override
    public void approva(int idRichiesta, String nota, SessioneUtenteBean sessioneGestore) {
        valuta(idRichiesta, true, nota, sessioneGestore);
    }

    @Override
    public void rifiuta(int idRichiesta, String nota, SessioneUtenteBean sessioneGestore) {
        valuta(idRichiesta, false, nota, sessioneGestore);
    }

    private void valuta(int idRichiesta, boolean approva, String nota, SessioneUtenteBean sessioneGestore) {
        if (isIdNonValido(idRichiesta, "Id richiesta non valido")
                || isSessioneNonValida(sessioneGestore, GraphicControllerUtils.MSG_SESSIONE_NON_VALIDA)) {
            return;
        }

        try {
            EsitoOperazioneBean esito =
                    logicController().valutaRichiestaDisdetta(idRichiesta, approva, nota, sessioneGestore);

            if (esito == null || !esito.isSuccesso()) {
                notifyError(esito != null ? esito.getMessaggio()
                        : GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
                return;
            }

            if (navigator != null) {
                navigator.goTo(
                        GraphicControllerUtils.ROUTE_RICHIESTE_DISDETTA,
                        Map.of(GraphicControllerUtils.KEY_SUCCESSO, esito.getMessaggio())
                );
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore valutazione richiesta disdetta", e);
            notifyError("Errore valutazione richiesta disdetta");
        }
    }

    @Override
    public void tornaAllaHome() {
        goToHome();
    }

    // ===== helper =====

    private void notifyError(String message) {
        GraphicControllerUtils.notifyError(
                log(),
                navigator,
                GraphicControllerUtils.ROUTE_RICHIESTE_DISDETTA,
                GraphicControllerUtils.PREFIX_RICHIESTE_DISDETTA,
                message
        );
    }

    private boolean isSessioneNonValida(SessioneUtenteBean sessione, String message) {
        if (sessione == null || sessione.getUtente() == null) {
            notifyError(message);
            return true;
        }
        if (sessione.getUtente().getRuolo() != com.ispw.model.enums.Ruolo.GESTORE) {
            notifyError("Operazione riservata al gestore");
            return true;
        }
        return false;
    }

    private boolean isIdNonValido(int idRichiesta, String message) {
        if (idRichiesta <= 0) {
            notifyError(message);
            return true;
        }
        return false;
    }
}