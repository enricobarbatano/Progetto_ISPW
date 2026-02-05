package com.ispw.controller.logic.ctrl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.PenalitaBean;
import com.ispw.bean.RegolaCampoBean;
import com.ispw.bean.TempisticheBean;
import com.ispw.controller.logic.interfaces.disponibilita.GestioneDisponibilitaGestioneRegole;
import com.ispw.controller.logic.interfaces.manutenzione.GestioneManutenzioneConfiguraRegole;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaConfiguraRegole;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.CampoDAO;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.dao.interfaces.RegolePenalitaDAO;
import com.ispw.dao.interfaces.RegoleTempisticheDAO;
import com.ispw.model.entity.Campo;
import com.ispw.model.entity.RegolePenalita;
import com.ispw.model.entity.RegoleTempistiche;
import com.ispw.model.entity.SystemLog;

/**
 * Controller applicativo per la configurazione delle regole.
 * - Stateless, DIP by-parameter per i collaboratori secondari.
 * - Nessun SQL: uso solo DAO dalla factory.
 * - Early-return, messaggi centralizzati, logger on-demand (Sonar-friendly).
 */
public class LogicControllerConfiguraRegole {

    // ========================
    // Messaggi centralizzati
    // ========================
    private static final String MSG_INPUT_CAMPO_KO     = "Dati regola campo non validi";
    private static final String MSG_CAMPO_NOT_FOUND    = "Campo inesistente";
    private static final String MSG_UPDATE_CAMPO_OK    = "Regola campo aggiornata";

    private static final String MSG_INPUT_MANUT_KO     = "Dati manutenzione non validi";
    private static final String MSG_MANUT_OK           = "Manutenzione impostata per il campo";

    private static final String MSG_INPUT_TEMP_KO      = "Tempistiche non valide";
    private static final String MSG_UPDATE_TEMP_OK     = "Regole tempistiche aggiornate";

    private static final String MSG_INPUT_PEN_KO       = "Regole penalità non valide";
    private static final String MSG_UPDATE_PEN_OK      = "Regole penalità aggiornate";

    // ========================
    // Logger on-demand (S1312)
    // ========================
    @SuppressWarnings("java:S1312")
    private Logger log() { return Logger.getLogger(getClass().getName()); }

    // ========================
    // DAO accessors (no concreti)
    // ========================
    private CampoDAO campoDAO() {
        return DAOFactory.getInstance().getCampoDAO();
    }
    private RegoleTempisticheDAO tempDAO() {
        return DAOFactory.getInstance().getRegoleTempisticheDAO();
    }
    private RegolePenalitaDAO penDAO() {
        return DAOFactory.getInstance().getRegolePenalitaDAO();
    }
    private LogDAO logDAO() {
        return DAOFactory.getInstance().getLogDAO();
    }

    // =====================================================================================
    // 0) Lista campi (supporto UI)
    // =====================================================================================

    /**
     * Restituisce una lista di campi in formato testuale per la selezione UI.
     */
    public List<String> listaCampi() {
        return campoDAO().findAll().stream()
            .map(c -> String.format("#%d - %s (%s) [attivo=%s, manutenzione=%s]",
                c.getIdCampo(),
                c.getNome(),
                c.getTipoSport(),
                c.isAttivo(),
                c.isFlagManutenzione()))
            .toList();
    }

    // =====================================================================================
    // 1) Aggiorna stato operativo campo (attivo/manutenzione)
    // =====================================================================================

    /** Aggiorna lo stato operativo del campo (attivo/manutenzione). Orchestrazione opzionale negli overload. */
    public EsitoOperazioneBean aggiornaRegoleCampo(RegolaCampoBean bean) {
        if (!isValid(bean)) {
            return esito(false, MSG_INPUT_CAMPO_KO);
        }
        final int idCampo = bean.getIdCampo();
        final Campo c = campoDAO().findById(idCampo);
        if (c == null) {
            return esito(false, MSG_CAMPO_NOT_FOUND);
        }

        boolean attivo = Boolean.TRUE.equals(bean.getAttivo());
        boolean manut = Boolean.TRUE.equals(bean.getFlagManutenzione());

        updateCampoOperativo(c, attivo, manut);
        campoDAO().store(c);

        appendLogSafe(String.format("[REGOLE] Aggiornato campo id=%d attivo=%s manutenzione=%s", idCampo, attivo, manut));
        return esito(true, MSG_UPDATE_CAMPO_OK);
    }

    /**
     * Overload con orchestrazione DIP:
     * - Disponibilità: attiva se (attivo && !manutenzione), altrimenti rimuovi.
     * - Manutenzione: se manutenzione==true → invio alert manutentore.
     * - Notifica: broadcast aggiornamento regole.
     */
    public EsitoOperazioneBean aggiornaRegoleCampo(RegolaCampoBean bean,
                                                   GestioneDisponibilitaGestioneRegole dispCtrl,
                                                   GestioneManutenzioneConfiguraRegole manCtrl,
                                                   GestioneNotificaConfiguraRegole notiCtrl) {
        EsitoOperazioneBean base = aggiornaRegoleCampo(bean);
        if (!base.isSuccesso()) return base;

        final int idCampo = bean.getIdCampo();
        final boolean attivo = Boolean.TRUE.equals(bean.getAttivo());
        final boolean manut = Boolean.TRUE.equals(bean.getFlagManutenzione());

        // Disponibilità
        if (dispCtrl != null) {
            try {
                if (attivo && !manut) {
                    dispCtrl.attivaDisponibilita(idCampo);
                } else {
                    dispCtrl.rimuoviDisponibilita(idCampo);
                }
            } catch (RuntimeException ex) {
                log().log(Level.FINE, "Aggiorna disponibilità fallito: {0}", ex.getMessage());
            }
        }

        // Manutenzione
        if (manut && manCtrl != null) {
            try {
                manCtrl.inviaAlertManutentore(idCampo);
            } catch (RuntimeException ex) {
                log().log(Level.FINE, "Alert manutenzione fallito: {0}", ex.getMessage());
            }
        }

        // Notifica broadcast
        generaNotificaAutomatica(notiCtrl);

        return base;
    }

    // =====================================================================================
    // 2) Esegui manutenzione (forza flag manutenzione e rimuove disponibilità)
    // =====================================================================================

    public EsitoOperazioneBean eseguiManutenzione(RegolaCampoBean bean) {
        if (bean == null || bean.getIdCampo() <= 0) {
            return esito(false, MSG_INPUT_MANUT_KO);
        }

        final Campo c = campoDAO().findById(bean.getIdCampo());
        if (c == null) {
            return esito(false, MSG_CAMPO_NOT_FOUND);
        }

        // Imposta manutenzione true e disattiva il campo
        updateCampoOperativo(c, false, true);
        campoDAO().store(c);

        appendLogSafe(String.format("[REGOLE] Impostata manutenzione su campo id=%d", bean.getIdCampo()));
        return esito(true, MSG_MANUT_OK);
    }

    /** Overload con orchestrazione: rimuove disponibilità, avvisa manutentore, broadcast. */
    public EsitoOperazioneBean eseguiManutenzione(RegolaCampoBean bean,
                                                  GestioneDisponibilitaGestioneRegole dispCtrl,
                                                  GestioneManutenzioneConfiguraRegole manCtrl,
                                                  GestioneNotificaConfiguraRegole notiCtrl) {
        EsitoOperazioneBean base = eseguiManutenzione(bean);
        if (!base.isSuccesso()) return base;

        final int idCampo = bean.getIdCampo();

        // Disponibilità → rimuovi
        if (dispCtrl != null) {
            try { dispCtrl.rimuoviDisponibilita(idCampo); }
            catch (RuntimeException ex) { log().log(Level.FINE, "Rimozione disponibilità fallita: {0}", ex.getMessage()); }
        }

        // Manutenzione → alert
        if (manCtrl != null) {
            try { manCtrl.inviaAlertManutentore(idCampo); }
            catch (RuntimeException ex) { log().log(Level.FINE, "Alert manutenzione fallito: {0}", ex.getMessage()); }
        }

        // Notifica broadcast
        generaNotificaAutomatica(notiCtrl);

        return base;
    }

    // =====================================================================================
    // 3) Aggiorna Regole Tempistiche
    // =====================================================================================

    public EsitoOperazioneBean aggiornaRegolaTempistiche(TempisticheBean bean) {
        if (!isValid(bean)) {
            return esito(false, MSG_INPUT_TEMP_KO);
        }

        RegoleTempistiche rt = new RegoleTempistiche();
        rt.setDurataSlot(bean.getDurataSlotMinuti());
        rt.setOraApertura(bean.getOraApertura());
        rt.setOraChiusura(bean.getOraChiusura());
        rt.setPreavvisoMinimo(bean.getPreavvisoMinimoMinuti());

        try {
            tempDAO().save(rt);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Salvataggio regole tempistiche fallito: {0}", ex.getMessage());
            return esito(false, MSG_INPUT_TEMP_KO);
        }

        appendLogSafe(String.format("[REGOLE] Tempistiche aggiornate: durata=%d' %s-%s, preavviso=%d'",
                rt.getDurataSlot(), rt.getOraApertura(), rt.getOraChiusura(), rt.getPreavvisoMinimo()));
        return esito(true, MSG_UPDATE_TEMP_OK);
    }

    /** Overload con notifica broadcast. */
    public EsitoOperazioneBean aggiornaRegolaTempistiche(TempisticheBean bean, GestioneNotificaConfiguraRegole notiCtrl) {
        EsitoOperazioneBean base = aggiornaRegolaTempistiche(bean);
        if (base.isSuccesso()) {
            generaNotificaAutomatica(notiCtrl);
        }
        return base;
    }

    // =====================================================================================
    // 4) Aggiorna Regole Penalità
    // =====================================================================================

    public EsitoOperazioneBean aggiornaRegolepenalita(PenalitaBean bean) {
        if (!isValid(bean)) {
            return esito(false, MSG_INPUT_PEN_KO);
        }

        RegolePenalita rp = new RegolePenalita();
        rp.setValorePenalita(bean.getValorePenalita());
        rp.setPreavvisoMinimo(bean.getPreavvisoMinimoMinuti());

        try {
            penDAO().save(rp);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Salvataggio regole penalità fallito: {0}", ex.getMessage());
            return esito(false, MSG_INPUT_PEN_KO);
        }

        appendLogSafe(String.format("[REGOLE] Penalità aggiornata: valore=%s, preavviso=%d'",
                rp.getValorePenalita(), rp.getPreavvisoMinimo()));
        return esito(true, MSG_UPDATE_PEN_OK);
    }

    /** Overload con notifica broadcast. */
    public EsitoOperazioneBean aggiornaRegolepenalita(PenalitaBean bean, GestioneNotificaConfiguraRegole notiCtrl) {
        EsitoOperazioneBean base = aggiornaRegolepenalita(bean);
        if (base.isSuccesso()) {
            generaNotificaAutomatica(notiCtrl);
        }
        return base;
    }

    // ========================
    // Helper (validazioni, log, reflection-safe updates)
    // ========================

    // ---> FIX 1: forma condensata, elimina if ridondanti
    private boolean isValid(RegolaCampoBean b) {
        return b != null
            && b.getIdCampo() > 0
            && b.getAttivo() != null
            && b.getFlagManutenzione() != null;
    }

    // ---> FIX 2: forma condensata, elimina if ridondanti
    private boolean isValid(TempisticheBean b) {
        return b != null
            && b.getDurataSlotMinuti() > 0
            && b.getOraApertura() != null
            && b.getOraChiusura() != null
            && b.getOraApertura().isBefore(b.getOraChiusura())
            && b.getPreavvisoMinimoMinuti() >= 0;
    }

    // ---> FIX 3: forma condensata, elimina if ridondanti
    private boolean isValid(PenalitaBean b) {
        return b != null
            && b.getValorePenalita() != null
            && b.getValorePenalita().signum() > 0
            && b.getPreavvisoMinimoMinuti() >= 0;
    }

    private EsitoOperazioneBean esito(boolean ok, String msg) {
        EsitoOperazioneBean e = new EsitoOperazioneBean();
        e.setSuccesso(ok);
        e.setMessaggio(msg);
        return e;
    }

    /** Best-effort log: evita eccezioni a runtime. */
    private void appendLogSafe(String descr) {
        try {
            SystemLog l = new SystemLog();
            l.setTimestamp(LocalDateTime.now());
            l.setIdUtenteCoinvolto(0); // sistema
            l.setDescrizione(Objects.toString(descr, ""));
            logDAO().append(l);
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Append log REGOLE fallito: {0}", ex.getMessage());
        }
    }

    /** Invoca la broadcast di aggiornamento regole se disponibile. */
    private void generaNotificaAutomatica(GestioneNotificaConfiguraRegole notiCtrl) {
        if (notiCtrl == null) return;
        try {
            notiCtrl.inviaNotificaAggiornamentoRegole();
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Notifica aggiornamento regole fallita: {0}", ex.getMessage());
        }
    }

    /**
     * Aggiorna il campo in modo compatibile con possibili differenze di naming
     * (setIsAttivo/setAttivo, setFlagManutenzione/setManutenzione) usando reflection;
     * se non riesce, tenta i field diretti.
     */
    private void updateCampoOperativo(Campo c, Boolean attivo, Boolean manut) {
        if (c == null) return;

        boolean doneAtt = invokeSetter(c, "setIsAttivo", Boolean.class, attivo)
                || invokeSetter(c, "setAttivo", Boolean.class, attivo);
        boolean doneMan = invokeSetter(c, "setFlagManutenzione", Boolean.class, manut)
                || invokeSetter(c, "setManutenzione", Boolean.class, manut);

        if (!doneAtt) {
            setFieldIfExists(c, "isAttivo", attivo);
            setFieldIfExists(c, "attivo", attivo);
        }
        if (!doneMan) {
            setFieldIfExists(c, "flagManutenzione", manut);
            setFieldIfExists(c, "manutenzione", manut);
        }
    }

    private boolean invokeSetter(Object target, String method, Class<?> argType, Object value) {
        try {
            Method m = target.getClass().getMethod(method, argType);
            m.setAccessible(true);
            m.invoke(target, value);
            return true;
        } catch (ReflectiveOperationException ex) {
            // Reflection may fail for many reasons (method not present or invocation error) — log at FINE for diagnostics
            log().log(Level.FINE, "invokeSetter failed: method={0} target={1} cause={2}", new Object[]{method, target.getClass().getName(), ex.getMessage()});
            return false;
        }
    }

    private void setFieldIfExists(Object target, String field, Object value) {
        if (target == null) return;
        try {
            Optional<Field> maybe = findField(target.getClass(), field);
            if (maybe.isPresent()) {
                Field f = maybe.get();
                f.setAccessible(true);
                f.set(target, value);
            }
        } catch (ReflectiveOperationException ex) {
            log().log(Level.FINE, "setFieldIfExists failed: field={0} target={1} cause={2}", new Object[]{field, target.getClass().getName(), ex.getMessage()});
        }
    }

    /** Cerca il field dichiarato nella gerarchia di classi; restituisce Optional.empty() se non trovato. */
    private Optional<Field> findField(Class<?> clazz, String fieldName) {
        Class<?> c = clazz;
        while (c != null) {
            try {
                Field f = c.getDeclaredField(fieldName);
                return Optional.of(f);
            } catch (NoSuchFieldException ex) {
                c = c.getSuperclass();
            }
        }
        return Optional.empty();
    }
}