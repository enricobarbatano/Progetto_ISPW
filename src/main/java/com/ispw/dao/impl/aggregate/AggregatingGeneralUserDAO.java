package com.ispw.dao.impl.aggregate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.ispw.dao.interfaces.GeneralUserDAO;
import com.ispw.dao.interfaces.GestoreDAO;
import com.ispw.dao.interfaces.UtenteFinaleDAO;
import com.ispw.model.entity.GeneralUser;
import com.ispw.model.entity.Gestore;
import com.ispw.model.entity.UtenteFinale;

public final class AggregatingGeneralUserDAO implements GeneralUserDAO {

    private static final Comparator<GeneralUser> ORDER_BY_ID_ASC =
            Comparator.comparingInt(GeneralUser::getIdUtente);

    private final GestoreDAO gestoreDAO;
    private final UtenteFinaleDAO utenteFinaleDAO;

    public AggregatingGeneralUserDAO(GestoreDAO gestoreDAO, UtenteFinaleDAO utenteFinaleDAO) {
        this.gestoreDAO = gestoreDAO;
        this.utenteFinaleDAO = utenteFinaleDAO;
    }

    /* ===================== FINDERS (polimorfismo runtime) ===================== */
    //questi metodi permettono di richiamare runtime i metodi su gestore o utente finale a seconda del tipo di utente che si sta cercando,
    //e sono utilizzati principalmente per login e per esportare tutti gli utenti finali/gestori

    @Override
    public GeneralUser findByEmail(String email) {
        final String norm = normalizeEmail(email);
        if (norm == null || norm.isBlank()) return null;

        Gestore g = gestoreDAO.findByEmail(norm);
        if (g != null) return g;

        return utenteFinaleDAO.findByEmail(norm);
    }

    @Override
    public GeneralUser findById(int idUtente) {
        if (idUtente <= 0) return null;

        Gestore g = gestoreDAO.findById(idUtente);
        if (g != null) return g;

        return utenteFinaleDAO.findById(idUtente);
    }

    @Override
    public List<GeneralUser> findAll() {
        // Merge gestori + utenti finali (dedupe per id, gestore ha precedenza se collisione)
        Map<Integer, GeneralUser> byId = new LinkedHashMap<>();

        List<Gestore> gestori = gestoreDAO.findAll();
        if (gestori != null) {
            for (Gestore g : gestori) {
                if (g != null && g.getIdUtente() > 0) byId.put(g.getIdUtente(), g);
            }
        }

        List<UtenteFinale> utenti = utenteFinaleDAO.findAll();
        if (utenti != null) {
            for (UtenteFinale u : utenti) {
                if (u != null && u.getIdUtente() > 0) byId.putIfAbsent(u.getIdUtente(), u);
            }
        }

        List<GeneralUser> out = new ArrayList<>(byId.values());
        out.sort(ORDER_BY_ID_ASC);
        return out;
    }

    /* ===================== DAO CRUD (instradamento corretto) ===================== */

    @Override
    public GeneralUser load(Integer id) {
        if (id == null || id <= 0) return null;
        return findById(id);
    }

    @Override
    public boolean exists(Integer id) {
        if (id == null || id <= 0) return false;
        // check “cheap”: prova gestore, poi utente finale
        if (gestoreDAO.exists(id)) return true;
        return utenteFinaleDAO.exists(id);
    }

    @Override
    public GeneralUser create(Integer id) {
        // Per compatibilità col tuo vecchio BaseGeneralUserDAO: default = UtenteFinale
        // (Se vuoi creare un Gestore, usa GestoreDAO.create direttamente)
        return utenteFinaleDAO.create(id);
    }

    @Override
    public void store(GeneralUser entity) {
        if (entity == null) return;

        // Instrada in base al tipo runtime
        if (entity instanceof Gestore g) {
            gestoreDAO.store(g);
            return;
        }

        if (entity instanceof UtenteFinale u) {
            utenteFinaleDAO.store(u);
            return;
        }

        throw new IllegalArgumentException(
            "Tipo utente non supportato da GeneralUserDAO facade: " + entity.getClass().getName()
        );
    }

    @Override
    public void delete(Integer id) {
        if (id == null || id <= 0) return;

        // Cancella dove esiste (prima gestore, poi utente finale)
        if (gestoreDAO.exists(id)) {
            gestoreDAO.delete(id);
            return;
        }
        if (utenteFinaleDAO.exists(id)) {
            utenteFinaleDAO.delete(id);
        }
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}