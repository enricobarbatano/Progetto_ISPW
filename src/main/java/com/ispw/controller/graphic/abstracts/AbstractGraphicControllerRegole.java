package com.ispw.controller.graphic.abstracts;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.CampiBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.PenalitaBean;
import com.ispw.bean.RegolaCampoBean;
import com.ispw.bean.TempisticheBean;
import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerRegole;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.logic.ctrl.LogicControllerConfiguraRegole;

/**
 * Classe astratta che centralizza la logica comune dei controller grafici Regole
 * (CLI/GUI) per ridurre duplicazione. Non introduce nuove responsabilità né
 * modifica il disaccoppiamento: delega invariata ai LogicController e mantiene
 * la stessa navigazione verso la View tramite GraphicControllerNavigation.
 */
public abstract class AbstractGraphicControllerRegole implements GraphicControllerRegole {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: implementa GraphicControllerRegole (interfaccia) e usa GraphicControllerNavigation.
    // A2) IO verso GUI/CLI: riceve/ritorna bean (CampiBean, RegolaCampoBean, TempisticheBean, PenalitaBean).
    // A3) Logica delegata: usa LogicControllerConfiguraRegole.
    // ========================

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicControllerRegole(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    protected abstract Logger log();

    protected abstract void goToHome();

    protected LogicControllerConfiguraRegole logicController() {
        return new LogicControllerConfiguraRegole();
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_REGOLE;
    }

    @Override
    public void onShow(Map<String, Object> params) {
    }

    @Override
    public void richiediListaCampi() {
        try {
            List<String> campi = formatCampi(logicController().listaCampi());
            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_REGOLE,
                    Map.of(GraphicControllerUtils.KEY_CAMPI, campi));
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore recupero lista campi", e);
        }
    }

    @Override
    public void selezionaCampo(int idCampo) {
        if (isIdCampoNonValido(idCampo)) {
            return;
        }
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_REGOLE,
                Map.of(GraphicControllerUtils.KEY_ID_CAMPO, idCampo));
        }
    }

    @Override
    public void aggiornaStatoCampo(Map<String, Object> regolaCampo) {
        if (isNullParams(regolaCampo, GraphicControllerUtils.MSG_PARAMETRI_REGOLA_CAMPO_MANCANTI)) {
            return;
        }

        RegolaCampoBean bean = buildRegolaCampoBean(regolaCampo);

        EsitoOperazioneBean esito = logicController().aggiornaRegoleCampo(bean);

        if (esito != null && esito.isSuccesso()) {
            navigateSuccess(esito.getMessaggio());
        } else {
            notifyRegoleError(esito != null ? esito.getMessaggio()
                : GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
        }
    }

    @Override
    public void aggiornaTempistiche(Map<String, Object> tempistiche) {
        if (isNullParams(tempistiche, GraphicControllerUtils.MSG_PARAMETRI_TEMPISTICHE_MANCANTI)) {
            return;
        }

        TempisticheBean bean = buildTempisticheBean(tempistiche);

        EsitoOperazioneBean esito = logicController().aggiornaRegolaTempistiche(bean);

        if (esito != null && esito.isSuccesso()) {
            navigateSuccess(esito.getMessaggio());
        } else {
            notifyRegoleError(esito != null ? esito.getMessaggio()
                : GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
        }
    }

    @Override
    public void aggiornaPenalita(Map<String, Object> penalita) {
        if (isNullParams(penalita, GraphicControllerUtils.MSG_PARAMETRI_PENALITA_MANCANTI)) {
            return;
        }

        PenalitaBean bean = buildPenalitaBean(penalita);

        EsitoOperazioneBean esito = logicController().aggiornaRegolepenalita(bean);

        if (esito != null && esito.isSuccesso()) {
            navigateSuccess(esito.getMessaggio());
        } else {
            notifyRegoleError(esito != null ? esito.getMessaggio()
                : GraphicControllerUtils.MSG_OPERAZIONE_NON_RIUSCITA);
        }
    }

    @Override
    public void tornaAllaHome() {
        goToHome();
    }

    // ========================
    // SEZIONE LOGICA
    // Legenda metodi:
    // 1) notifyRegoleError(...) - notifica errore e naviga.
    // 2) isIdCampoNonValido(...) - valida id campo.
    // 3) isNullParams(...) - valida parametri.
    // 4) navigateSuccess(...) - naviga con successo.
    // 5) buildRegolaCampoBean(...) - costruisce bean regola campo.
    // 6) formatCampi(...) - formatta lista campi.
    // 7) buildTempisticheBean(...) - costruisce bean tempistiche.
    // 8) buildPenalitaBean(...) - costruisce bean penalita.
    // ========================
    private void notifyRegoleError(String message) {
        GraphicControllerUtils.notifyError(log(), navigator, GraphicControllerUtils.ROUTE_REGOLE,
            GraphicControllerUtils.PREFIX_REGOLE, message);
    }

    private boolean isIdCampoNonValido(int idCampo) {
        if (idCampo <= 0) {
            notifyRegoleError(GraphicControllerUtils.MSG_ID_CAMPO_NON_VALIDO);
            return true;
        }
        return false;
    }

    private boolean isNullParams(Map<String, Object> params, String message) {
        if (params == null) {
            notifyRegoleError(message);
            return true;
        }
        return false;
    }

    private void navigateSuccess(String message) {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_REGOLE,
                Map.of(GraphicControllerUtils.KEY_SUCCESSO, message));
        }
    }

    private RegolaCampoBean buildRegolaCampoBean(Map<String, Object> regolaCampo) {
        RegolaCampoBean bean = new RegolaCampoBean();
        if (regolaCampo.containsKey(GraphicControllerUtils.KEY_ID_CAMPO)) {
            bean.setIdCampo((Integer) regolaCampo.get(GraphicControllerUtils.KEY_ID_CAMPO));
        }
        if (regolaCampo.containsKey(GraphicControllerUtils.KEY_ATTIVO)) {
            bean.setAttivo((Boolean) regolaCampo.get(GraphicControllerUtils.KEY_ATTIVO));
        }
        if (regolaCampo.containsKey(GraphicControllerUtils.KEY_FLAG_MANUTENZIONE)) {
            bean.setFlagManutenzione((Boolean) regolaCampo.get(GraphicControllerUtils.KEY_FLAG_MANUTENZIONE));
        }
        return bean;
    }

    private List<String> formatCampi(CampiBean campi) {
        if (campi == null || campi.getCampi() == null || campi.getCampi().isEmpty()) {
            return List.of("Nessun campo disponibile");
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

    private TempisticheBean buildTempisticheBean(Map<String, Object> tempistiche) {
        TempisticheBean bean = new TempisticheBean();
        if (tempistiche.containsKey(GraphicControllerUtils.KEY_PREAVVISO_MINIMO_MINUTI)) {
            bean.setPreavvisoMinimoMinuti((Integer) tempistiche.get(
                GraphicControllerUtils.KEY_PREAVVISO_MINIMO_MINUTI));
        }
        if (tempistiche.containsKey(GraphicControllerUtils.KEY_DURATA_SLOT_MINUTI)) {
            bean.setDurataSlotMinuti((Integer) tempistiche.get(GraphicControllerUtils.KEY_DURATA_SLOT_MINUTI));
        }
        if (tempistiche.containsKey(GraphicControllerUtils.KEY_ORA_APERTURA)) {
            bean.setOraApertura((LocalTime) tempistiche.get(GraphicControllerUtils.KEY_ORA_APERTURA));
        }
        if (tempistiche.containsKey(GraphicControllerUtils.KEY_ORA_CHIUSURA)) {
            bean.setOraChiusura((LocalTime) tempistiche.get(GraphicControllerUtils.KEY_ORA_CHIUSURA));
        }
        return bean;
    }

    private PenalitaBean buildPenalitaBean(Map<String, Object> penalita) {
        PenalitaBean bean = new PenalitaBean();
        if (penalita.containsKey(GraphicControllerUtils.KEY_PREAVVISO_MINIMO_MINUTI)) {
            bean.setPreavvisoMinimoMinuti((Integer) penalita.get(
                GraphicControllerUtils.KEY_PREAVVISO_MINIMO_MINUTI));
        }
        if (penalita.containsKey(GraphicControllerUtils.KEY_VALORE_PENALITA)) {
            bean.setValorePenalita((BigDecimal) penalita.get(GraphicControllerUtils.KEY_VALORE_PENALITA));
        }
        return bean;
    }
}
