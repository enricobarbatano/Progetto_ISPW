package com.ispw.view.interfaces;

import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;

public interface GenericView {

	// SEZIONE ARCHITETTURALE
	// Legenda architettura:
	// A1) Collaboratori: contratto base per le view (CLI/GUI).
	// A2) IO verso controller: usa Map e SessioneUtenteBean.

	String KEY_ERROR = "error";
	String KEY_SUCCESSO = "successo";
	String KEY_MESSAGE = "message";
	String KEY_SESSIONE = "sessione";

	default void onShow() { onShow(Map.of()); }

	void onShow(Map<String, Object> params);

	default void onHide() { /* opzionale */ }

	// SEZIONE LOGICA
	// Legenda logica:
	// L1) Lifecycle: onShow/onHide.
	// L2) readSession/readError/readSuccess: estrazione dati dai params.

	default SessioneUtenteBean readSession(Map<String, Object> params) {
		if (params == null) {
			return null;
		}
		Object raw = params.get(KEY_SESSIONE);
		return (raw instanceof SessioneUtenteBean s) ? s : null;
	}

	default String readError(Map<String, Object> params) {
		if (params == null) {
			return null;
		}
		Object raw = params.get(KEY_ERROR);
		return raw == null ? null : String.valueOf(raw);
	}

	default String readSuccess(Map<String, Object> params) {
		if (params == null) {
			return null;
		}
		Object raw = params.get(KEY_MESSAGE);
		if (raw == null) {
			raw = params.get(KEY_SUCCESSO);
		}
		return raw == null ? null : String.valueOf(raw);
	}
}
