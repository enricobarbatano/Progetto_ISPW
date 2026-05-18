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
import com.ispw.controller.graphic.interfaces.GraphicControllerNavigation;
import com.ispw.controller.graphic.interfaces.GraphicControllerRegole;
import com.ispw.controller.graphic.interfaces.GraphicControllerUtils;
import com.ispw.controller.logic.LogicControllerFactory;
import com.ispw.controller.logic.interfaces.CtrlGestioneRegole;

public abstract class AbstractGraphicControllerRegole implements GraphicControllerRegole {

    protected final GraphicControllerNavigation navigator;

    protected AbstractGraphicControllerRegole(GraphicControllerNavigation navigator) {
        this.navigator = navigator;
    }

    protected abstract Logger log();

    protected abstract void goToHome();

    protected CtrlGestioneRegole logicController() {
        return LogicControllerFactory.getGestioneRegoleController();
    }

    @Override
    public String getRouteName() {
        return GraphicControllerUtils.ROUTE_REGOLE;
    }

    @Override
    public void onShow(Map<String, Object> params) {
    }

    @Override
    public void aggiornaStatoCampo(int idCampo, boolean attivo, boolean manutenzione) {

        if (idCampo <= 0) {
            notifyError("Id campo non valido");
            return;
        }

        RegolaCampoBean bean = new RegolaCampoBean();
        bean.setIdCampo(idCampo);
        bean.setAttivo(attivo);
        bean.setFlagManutenzione(manutenzione);

        handleEsito(logicController().aggiornaRegoleCampo(bean));
    }

    @Override
    public void aggiornaTempistiche(int preavviso,
                                     int durataSlot,
                                     LocalTime apertura,
                                     LocalTime chiusura) {

        TempisticheBean bean = new TempisticheBean();
        bean.setPreavvisoMinimoMinuti(preavviso);
        bean.setDurataSlotMinuti(durataSlot);
        bean.setOraApertura(apertura);
        bean.setOraChiusura(chiusura);

        handleEsito(logicController().aggiornaRegolaTempistiche(bean));
    }

    @Override
    public void aggiornaPenalita(int preavviso,
                                 BigDecimal valore) {

        PenalitaBean bean = new PenalitaBean();
        bean.setPreavvisoMinimoMinuti(preavviso);
        bean.setValorePenalita(valore);

        handleEsito(logicController().aggiornaRegolepenalita(bean));
    }

    @Override
    public void richiediListaCampi() {

        try {
            CampiBean campi = logicController().listaCampi();

            List<String> lista = campi.getCampi().stream()
                    .map(c -> c.getIdCampo() + " - " + c.getNome())
                    .toList();

            navigator.goTo(
                    GraphicControllerUtils.ROUTE_REGOLE,
                    Map.of(GraphicControllerUtils.KEY_CAMPI, lista)
            );

        } catch (RuntimeException ex) {
            log().log(Level.SEVERE, "Errore campi", ex);
        }
    }

    @Override
    public void selezionaCampo(int idCampo) {
        navigator.goTo(
                GraphicControllerUtils.ROUTE_REGOLE,
                Map.of(GraphicControllerUtils.KEY_ID_CAMPO, idCampo)
        );
    }

    @Override
    public void tornaAllaHome() {
        goToHome();
    }

    private void handleEsito(EsitoOperazioneBean esito) {
        if (esito != null && esito.isSuccesso()) {
            navigator.goTo(
                    GraphicControllerUtils.ROUTE_REGOLE,
                    Map.of(GraphicControllerUtils.KEY_SUCCESSO, esito.getMessaggio())
            );
        } else {
            notifyError("Errore operazione");
        }
    }

    private void notifyError(String msg) {
        GraphicControllerUtils.notifyError(
                log(),
                navigator,
                GraphicControllerUtils.ROUTE_REGOLE,
                GraphicControllerUtils.PREFIX_REGOLE,
                msg
        );
    }
}