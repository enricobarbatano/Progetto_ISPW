
/**
 * Test class developed by Enrico Barbatano.
 */
package com.ispw.controller.logic.ctrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.ispw.bean.DatiAccountBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.bean.UtenteBean;
import com.ispw.model.enums.Ruolo;

class LogicControllerGestioneAccountTest {

    @Test
    @DisplayName("Recupera info account: sessione null -> null")
    void recuperaInformazioniAccountSessioneNullReturnNull() {
        var ctrl = new LogicControllerGestioneAccount();

        var result = ctrl.recuperaInformazioniAccount(null);

        assertNull(result);
    }

    @Test
    @DisplayName("Recupera info account: sessione.Utente null -> null")
    void recuperaInformazioniAccountSessioneUtenteNullReturnNull() {
        var ctrl = new LogicControllerGestioneAccount();
        var sessione = new SessioneUtenteBean();
        sessione.setUtente(null);

        var result = ctrl.recuperaInformazioniAccount(sessione);

        assertNull(result);
    }

    @Test
    @DisplayName("Recupera info account: email blank -> null")
    void recuperaInformazioniAccountEmailBlankReturnNull() {
        var ctrl = new LogicControllerGestioneAccount();
        var sessione = new SessioneUtenteBean();
        sessione.setUtente(new UtenteBean("Mario", "Rossi", "  ", Ruolo.UTENTE));

        var result = ctrl.recuperaInformazioniAccount(sessione);

        assertNull(result);
    }

    @Test
    @DisplayName("Cambia Password: sessione null -> null")
    void cambiaPasswordSessioneNullReturnNull() {
        var ctrl = new LogicControllerGestioneAccount();

        EsitoOperazioneBean result = ctrl.cambiaPassword("oldpwd", "newpwd", null);

        assertNotNull(result);
        assertFalse(result.isSuccesso());
        assertEquals("Sessione non valida", result.getMessaggio());
    }

    @ParameterizedTest(name = "Cambia Password input non valido: oldPwd=''{0}'', newPwd=''{1}'' -> Ko")
    @CsvSource({
            "'', newpwd",
            "oldpwd, ''",
            "oldpwd, oldpw"
    })
    @DisplayName("Cambia Password: password non valida -> Ko")
    void cambiaPasswordPasswordNonValidaReturnKo(String oldPassword, String newPassword) {
        var ctrl = new LogicControllerGestioneAccount();
        var sessione = new SessioneUtenteBean();
        sessione.setUtente(new UtenteBean("Mario", "Rossi", "mario.rossi@example.com", Ruolo.UTENTE));

        EsitoOperazioneBean result = ctrl.cambiaPassword(oldPassword, newPassword, sessione);

        assertNotNull(result);
        assertFalse(result.isSuccesso());
    }

    @Test
    @DisplayName("Aggiorna Dati Account: nuovi dati Null -> Ko")
    void aggiornaDatiAccountNuoviDatiNullReturnKo() {
        var ctrl = new LogicControllerGestioneAccount();

        var result = ctrl.aggiornaDatiAccount(null);

        assertFalse(result.isSuccesso());
        assertEquals("Dati non validi", result.getMessaggio());
    }

    @Test
    @DisplayName("Aggiorna Dati Account: id <= 0 -> Ko")
    void aggiornaDatiAccountIdUtenteMinoreDi0ReturnKo() {
        var ctrl = new LogicControllerGestioneAccount();
        var dati = new DatiAccountBean();
        dati.setIdUtente(0);

        var result = ctrl.aggiornaDatiAccount(dati);

        assertFalse(result.isSuccesso());
        assertEquals("Dati non validi", result.getMessaggio());
    }
}
