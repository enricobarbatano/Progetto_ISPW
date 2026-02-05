package com.ispw.controller.graphic.cli;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.controller.graphic.GraphicControllerNavigation;
import com.ispw.controller.graphic.GraphicControllerUtils;
import com.ispw.controller.graphic.abstracts.AbstractGraphicControllerPenalita;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.enums.Ruolo;

public class CLIGraphicControllerPenalita extends AbstractGraphicControllerPenalita {
    
    public CLIGraphicControllerPenalita(GraphicControllerNavigation navigator) {
        super(navigator);
    }

    @SuppressWarnings("java:S1312")
    @Override
    protected Logger log() { return Logger.getLogger(getClass().getName()); }

    @Override
    protected void goToHome() {
        if (navigator != null) {
            navigator.goTo(GraphicControllerUtils.ROUTE_HOME);
        }
    }

    public void richiediListaUtenti() {
        try {
            GeneralUserDAO userDAO = DAOFactory.getInstance().getGeneralUserDAO();
            List<String> utenti = userDAO.findAll().stream()
                .filter(u -> u != null && u.getRuolo() == Ruolo.UTENTE)
                .map(this::formatUtente)
                .toList();

            if (navigator != null) {
                navigator.goTo(GraphicControllerUtils.ROUTE_PENALITA,
                    Map.of(GraphicControllerUtils.KEY_UTENTI, utenti));
            }
        } catch (Exception e) {
            log().log(Level.SEVERE, "Errore recupero lista utenti", e);
        }
    }

    private String formatUtente(GeneralUser u) {
        String email = u.getEmail() != null ? u.getEmail() : "";
        return String.format("#%d - %s (%s)", u.getIdUtente(), email, u.getRuolo());
    }

}
