package com.ispw.view.interfaces;

import java.util.Map;

import com.ispw.bean.SessioneUtenteBean;

/**
 * Contratto base per le View (CLI/GUI).
 * Le view usano solo dati grezzi e {@link SessioneUtenteBean}.
 */
public interface GenericView {

	String KEY_ERROR = "error";
	String KEY_SUCCESSO = "successo";
	String KEY_MESSAGE = "message";
	String KEY_SESSIONE = "sessione";

	/** Lifecycle: mostrata senza parametri */
	default void onShow() { onShow(Map.of()); }

	/** Lifecycle: mostrata con parametri */
	void onShow(Map<String, Object> params);

	/** Lifecycle: in uscita dalla schermata (opzionale) */
	default void onHide() { /* opzionale */ }

	/** Estrae la sessione dai params (se presente). */
	default SessioneUtenteBean readSession(Map<String, Object> params) {
		if (params == null) {
			return null;
		}
		Object raw = params.get(KEY_SESSIONE);
		return (raw instanceof SessioneUtenteBean s) ? s : null;
	}

	/** Estrae un messaggio di errore, se presente. */
	default String readError(Map<String, Object> params) {
		if (params == null) {
			return null;
		}
		Object raw = params.get(KEY_ERROR);
		return raw == null ? null : String.valueOf(raw);
	}

	/** Estrae un messaggio di successo, se presente. */
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
