package com.ispw.bootstrap;

import java.util.List;

import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.CampoDAO;
import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.model.entity.Campo;
import com.ispw.model.entity.Gestore;
import com.ispw.model.enums.PersistencyProvider;
import com.ispw.model.enums.Ruolo;
import com.ispw.model.enums.StatoAccount;

public final class DataSeeder {

    private static final String DEFAULT_GESTORE_EMAIL = "gestore@ispw.local";
    private static final String DEFAULT_GESTORE_PASSWORD = "gestore123";
    private static final String DEFAULT_GESTORE_NOME = "Gestore";
    private static final String DEFAULT_GESTORE_COGNOME = "Sistema";

    private DataSeeder() {
        // utility class
    }

    public static void seedIfNeeded(PersistencyProvider provider) {
        if (provider != PersistencyProvider.FILE_SYSTEM) {
            return;
        }
        seedGestore();
        seedCampi();
    }

    private static void seedGestore() {
        GeneralUserDAO userDAO = DAOFactory.getInstance().getGeneralUserDAO();
        boolean hasGestore = userDAO.findAll().stream()
            .anyMatch(u -> u != null && u.getRuolo() == Ruolo.GESTORE);
        if (hasGestore) {
            return;
        }
        if (userDAO.findByEmail(DEFAULT_GESTORE_EMAIL) != null) {
            return;
        }

        Gestore g = new Gestore();
        g.setNome(DEFAULT_GESTORE_NOME);
        g.setCognome(DEFAULT_GESTORE_COGNOME);
        g.setEmail(DEFAULT_GESTORE_EMAIL);
        g.setPassword(DEFAULT_GESTORE_PASSWORD);
        g.setRuolo(Ruolo.GESTORE);
        g.setStatoAccount(StatoAccount.ATTIVO);
        userDAO.store(g);
    }

    private static void seedCampi() {
        CampoDAO campoDAO = DAOFactory.getInstance().getCampoDAO();
        List<Campo> existing = campoDAO.findAll();
        if (existing != null && !existing.isEmpty()) {
            return;
        }
        campoDAO.store(buildCampo(1, "Campo A", "Calcio", 25f, true, false));
        campoDAO.store(buildCampo(2, "Campo B", "Tennis", 18f, true, false));
        campoDAO.store(buildCampo(3, "Campo C", "Padel", 20f, true, false));
    }

    private static Campo buildCampo(int id, String nome, String sport, float costoOrario,
                                    boolean attivo, boolean manutenzione) {
        Campo c = new Campo();
        c.setIdCampo(id);
        c.setNome(nome);
        c.setTipoSport(sport);
        c.setCostoOrario(costoOrario);
        c.setAttivo(attivo);
        c.setFlagManutenzione(manutenzione);
        return c;
    }
}
