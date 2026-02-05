package com.ispw.view.gui;

import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;
import com.ispw.view.interfaces.GenericView;

/**
 * Base astratta per le view GUI.
 */
public abstract class GenericViewGUI implements GenericView {

	protected SessioneUtenteBean sessione;
	protected Map<String, Object> lastParams = Map.of();

	@Override
	public void onShow(Map<String, Object> params) {
		lastParams = (params == null) ? Map.of() : params;
		sessione = readSession(lastParams);
	}

	public SessioneUtenteBean getSessione() { return sessione; }

	public Map<String, Object> getLastParams() { return lastParams; }

	public String getLastError() { return readError(lastParams); }

	public String getLastSuccess() { return readSuccess(lastParams); }
}
