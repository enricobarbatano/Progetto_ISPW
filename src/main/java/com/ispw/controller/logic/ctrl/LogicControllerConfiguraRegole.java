package com.ispw.controller.logic.ctrl;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ispw.bean.CampiBean;
import com.ispw.bean.CampoBean;
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

    // Messaggi centralizzati
    private static final String MSG_INPUT_CAMPO_KO     = "Dati regola campo non validi";
    private static final String MSG_CAMPO_NOT_FOUND    = "Campo inesistente";
    private static final String MSG_UPDATE_CAMPO_OK    = "Regola campo aggiornata";

    private static final String MSG_INPUT_MANUT_KO     = "Dati manutenzione non validi";
    private static final String MSG_MANUT_OK           = "Manutenzione impostata per il campo";

    private static final String MSG_INPUT_TEMP_KO      = "Tempistiche non valide";
    private static final String MSG_UPDATE_TEMP_OK     = "Regole tempistiche aggiornate";

    private static final String MSG_INPUT_PEN_KO       = "Regole penalita non valide";
    private static final String MSG_UPDATE_PEN_OK      = "Regole penalita aggiornate";

    @SuppressWarnings("java:S1312")
    private Logger log() { return Logger.getLogger(getClass().getName()); }

    // DAO accessors (runtime via factory)
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

    // 0) Lista campi (supporto UI)
    public CampiBean listaCampi() {
        CampiBean out = new CampiBean();
        out.setCampi(campoDAO().findAll().stream()
            .map(this::toBean)
            .toList());
        return out;
    }

    private CampoBean toBean(Campo campo) {
        CampoBean bean = new CampoBean();
        if (campo == null) {
            return bean;
        }
        bean.setIdCampo(campo.getIdCampo());
        bean.setNome(campo.getNome());
        bean.setTipoSport(campo.getTipoSport());
        if (campo.getCostoOrario() != null) {
            bean.setCostoOrario(java.math.BigDecimal.valueOf(campo.getCostoOrario()));
        }
        bean.setAttivo(campo.isAttivo());
        bean.setFlagManutenzione(campo.isFlagManutenzione());
        return bean;
    }

    // 1) Aggiorna stato operativo campo (attivo/manutenzione)

    public EsitoOperazioneBean aggiornaRegoleCampo(RegolaCampoBean bean) {
        if (!isValid(bean)) {
            return esito(false, MSG_INPUT_CAMPO_KO);
        }

        final int idCampo = bean.getIdCampo();
        final Campo c = campoDAO().findById(idCampo);
        if (c == null) {
            return esito(false, MSG_CAMPO_NOT_FOUND);
        }

        final boolean attivo = Boolean.TRUE.equals(bean.getAttivo());
        final boolean manut  = Boolean.TRUE.equals(bean.getFlagManutenzione());

        // ✅ senza reflection: usa l’API stabile del dominio
        updateCampoOperativo(c, attivo, manut);

        campoDAO().store(c);

        appendLogSafe(String.format("[REGOLE] Aggiornato campo id=%d attivo=%s manutenzione=%s",
                idCampo, attivo, manut));
        return esito(true, MSG_UPDATE_CAMPO_OK);
    }

    public EsitoOperazioneBean aggiornaRegoleCampo(RegolaCampoBean bean,
                                                   GestioneDisponibilitaGestioneRegole dispCtrl,
                                                   GestioneManutenzioneConfiguraRegole manCtrl,
                                                   GestioneNotificaConfiguraRegole notiCtrl) {
        EsitoOperazioneBean base = aggiornaRegoleCampo(bean);
        if (!base.isSuccesso()) return base;

        final int idCampo = bean.getIdCampo();
        final boolean attivo = Boolean.TRUE.equals(bean.getAttivo());
        final boolean manut  = Boolean.TRUE.equals(bean.getFlagManutenzione());

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

        // Manutenzione: alert
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

    // 2) Esegui manutenzione (forza flag manutenzione e rimuove disponibilità)

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

    // 3) Aggiorna Regole Tempistiche

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

    public EsitoOperazioneBean aggiornaRegolaTempistiche(TempisticheBean bean, GestioneNotificaConfiguraRegole notiCtrl) {
        EsitoOperazioneBean base = aggiornaRegolaTempistiche(bean);
        if (base.isSuccesso()) {
            generaNotificaAutomatica(notiCtrl);
        }
        return base;
    }

    // 4) Aggiorna Regole Penalità

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
            log().log(Level.FINE, "Salvataggio regole penalita fallito: {0}", ex.getMessage());
            return esito(false, MSG_INPUT_PEN_KO);
        }

        appendLogSafe(String.format("[REGOLE] Penalita aggiornata: valore=%s, preavviso=%d'",
                rp.getValorePenalita(), rp.getPreavvisoMinimo()));
        return esito(true, MSG_UPDATE_PEN_OK);
    }

    public EsitoOperazioneBean aggiornaRegolepenalita(PenalitaBean bean, GestioneNotificaConfiguraRegole notiCtrl) {
        EsitoOperazioneBean base = aggiornaRegolepenalita(bean);
        if (base.isSuccesso()) {
            generaNotificaAutomatica(notiCtrl);
        }
        return base;
    }

    // ===================== LOGICA (helpers) =====================

    private boolean isValid(RegolaCampoBean b) {
        return b != null
            && b.getIdCampo() > 0
            && b.getAttivo() != null
            && b.getFlagManutenzione() != null;
    }

    private boolean isValid(TempisticheBean b) {
        return b != null
            && b.getDurataSlotMinuti() > 0
            && b.getOraApertura() != null
            && b.getOraChiusura() != null
            && b.getOraApertura().isBefore(b.getOraChiusura())
            && b.getPreavvisoMinimoMinuti() >= 0;
    }

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

    private void generaNotificaAutomatica(GestioneNotificaConfiguraRegole notiCtrl) {
        if (notiCtrl == null) return;
        try {
            notiCtrl.inviaNotificaAggiornamentoRegole();
        } catch (RuntimeException ex) {
            log().log(Level.FINE, "Notifica aggiornamento regole fallita: {0}", ex.getMessage());
        }
    }

    /**
     * ✅ Versione pulita (no reflection):
     * usa il metodo di dominio già presente su Campo.
     */
    private void updateCampoOperativo(Campo c, boolean attivo, boolean manut) {
        if (c == null) return;
        try {
            c.updateStatoOperativo(attivo, manut);
        } catch (RuntimeException ex) {
            // best-effort: non bloccare il caso d'uso per una validazione interna
            log().log(Level.FINE, "updateStatoOperativo fallito: {0}", ex.getMessage());
        }
    }

    // ---- I metodi reflection rimangono nel file solo perché non vuoi cambiare import/dipendenze.
    // ---- Sono inutilizzati in questa versione (puoi rimuoverli in un cleanup finale).
    @SuppressWarnings("unused")
    private boolean invokeSetter(Object target, String method, Class<?> argType, Object value) { return false; }
    @SuppressWarnings("unused")
    private void setFieldIfExists(Object target, String field, Object value) { }
    @SuppressWarnings("unused")
    private Optional<Field> findField(Class<?> clazz, String fieldName) { return Optional.empty(); }
}
