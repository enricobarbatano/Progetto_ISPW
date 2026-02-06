package com.ispw.view.common;

import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.view.interfaces.GenericView;

public abstract class GenericViewBase implements GenericView {

    // ========================
    // SEZIONE ARCHITETTURALE
    // Legenda architettura:
    // A1) Collaboratori: base comune per view CLI/GUI.
    // A2) IO: gestisce sessione e parametri di navigazione.
    // ========================

    protected SessioneUtenteBean sessione;
    protected Map<String, Object> lastParams = Map.of();

    // ========================
    // SEZIONE LOGICA
    // Legenda logica:
    // L1) onShow: aggiorna params e sessione.
    // L2) onShow/onHide: lifecycle base.
    // L3) getter: accesso a sessione, params e messaggi.
    // ========================

    @Override
    public void onShow(Map<String, Object> params) {
        lastParams = (params == null) ? Map.of() : params;
        if (lastParams.containsKey(KEY_SESSIONE)) {
            sessione = readSession(lastParams);
        }
    }

    @Override
    public void onShow() {
        onShow(Map.of());
    }

    @Override
    public void onHide() {
    }

    public SessioneUtenteBean getSessione() { return sessione; }

    public Map<String, Object> getLastParams() { return lastParams; }

    public String getLastError() { return readError(lastParams); }

    public String getLastSuccess() { return readSuccess(lastParams); }
}
