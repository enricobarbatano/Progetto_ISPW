package com.ispw.view.common;

import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.view.interfaces.GenericView;

/**
 * Base condivisa per le view (CLI/GUI).
 */
public abstract class GenericViewBase implements GenericView {

    protected SessioneUtenteBean sessione;
    protected Map<String, Object> lastParams = Map.of();

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
