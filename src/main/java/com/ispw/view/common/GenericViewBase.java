package com.ispw.view.common;

import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.view.interfaces.GenericView;

/**
 * Classe base comune per le view CLI e GUI.
 *
 * Responsabilità:
 * - conserva gli ultimi parametri ricevuti dal navigator;
 * - aggiorna la sessione quando viene passata nei parametri;
 * - espone helper per errore e successo.
 *
 * Nota:
 * se una navigazione non contiene KEY_SESSIONE, la sessione precedente
 * viene mantenuta. Questo permette a schermate come Home di restare
 * coerenti anche quando un controller torna alla home senza ripassare
 * esplicitamente la sessione.
 */
public abstract class GenericViewBase implements GenericView {

    protected SessioneUtenteBean sessione;
    protected Map<String, Object> lastParams = Map.of();

    @Override
    public void onShow(Map<String, Object> params) {
        lastParams = params != null ? params : Map.of();

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
        // Hook disponibile per eventuali view concrete.
    }

    public SessioneUtenteBean getSessione() {
        return sessione;
    }

    public Map<String, Object> getLastParams() {
        return lastParams;
    }

    public String getLastError() {
        return readError(lastParams);
    }

    public String getLastSuccess() {
        return readSuccess(lastParams);
    }
}