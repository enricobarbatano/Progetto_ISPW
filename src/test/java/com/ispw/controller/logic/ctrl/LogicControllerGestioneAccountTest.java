package com.ispw.controller.logic.ctrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ispw.bean.DatiAccountBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.SessioneUtenteBean;
import com.ispw.bean.UtenteBean;
import com.ispw.model.enums.Ruolo;

public class LogicControllerGestioneAccountTest {
    
    @Test
    @DisplayName("Recupera info account: sessione null -> null")
    void recuperaInformazioniAccount_sessioneNull_returnNull(){
        var ctrl= new LogicControllerGestioneAccount();
        var result=ctrl.recuperaInformazioniAccount(null);
        assertNull(result);
    }

    @Test
    @DisplayName("Recupera info account: sessione.Utente null -> null")
    void recuperaInformazioniAccount_sessioneUtenteNull_returnNull(){
        var ctrl= new LogicControllerGestioneAccount();
        var sessione= new SessioneUtenteBean();
        sessione.setUtente(null);
        var result=ctrl.recuperaInformazioniAccount(sessione);
        assertNull(result);
    }

    @Test
    @DisplayName("Recupera info account: email blank -> null")
    void recuperaInformazioniAccount_emailBlank_returnNull(){
        var ctrl= new LogicControllerGestioneAccount();
        var sessione= new SessioneUtenteBean();
        sessione.setUtente(new UtenteBean("Mario","Rossi","  ", Ruolo.UTENTE));
        var result=ctrl.recuperaInformazioniAccount(sessione);
        assertNull(result);
    }

    @Test
    @DisplayName("Cambia Password: sessione null -> null")
    void cambiaPassword_sessioneNull_returnNull(){
    var ctrl= new LogicControllerGestioneAccount();
    EsitoOperazioneBean result=ctrl.cambiaPassword("oldpwd", "newpwd", null);
    assertNotNull(result);
    assertFalse(result.isSuccesso());
    assertEquals("Sessione non valida", result.getMessaggio());
    }
    
    @Test
    @DisplayName("Cambia Password: vecchiaPWD blank -> Ko")
    void cambiaPassword_vecchiapwdBlank_returnKO(){
    var ctrl= new LogicControllerGestioneAccount();
    var sessione= new SessioneUtenteBean();
    sessione.setUtente(new UtenteBean("Mario","Rossi","mario.rossi@example.com", Ruolo.UTENTE));
    EsitoOperazioneBean result=ctrl.cambiaPassword(" ", "newpwd", sessione);
    assertNotNull(result);
    assertFalse(result.isSuccesso());
    }

    @Test
    @DisplayName("Cambia Password: nuovaPWD blank -> Ko")
    void cambiaPassword_nuovapwdBlank_returnKO(){
    var ctrl= new LogicControllerGestioneAccount();
    var sessione= new SessioneUtenteBean();
    sessione.setUtente(new UtenteBean("Mario","Rossi","mario.rossi@example.com", Ruolo.UTENTE));
    EsitoOperazioneBean result=ctrl.cambiaPassword("oldpwd", " ", sessione);
    assertNotNull(result);
    assertFalse(result.isSuccesso());
    }

    @Test
    @DisplayName("Cambia Password: nuovaPWD <6 -> Ko")
    void cambiaPassword_nuovapwdMinoreDi6Caratteri_returnKO(){
    var ctrl= new LogicControllerGestioneAccount();
    var sessione= new SessioneUtenteBean();
    sessione.setUtente(new UtenteBean("Mario","Rossi","mario.rossi@example.com", Ruolo.UTENTE));
    EsitoOperazioneBean result=ctrl.cambiaPassword("oldpwd", "oldpw", sessione);
    assertNotNull(result);
    assertFalse(result.isSuccesso());
    }

    @Test
    @DisplayName("Aggiorna Dati Account: nuovi dati Null->Ko")
    void aggiornaDatiAccount_nuoviDatiNull_returnKo(){
    var ctrl=new LogicControllerGestioneAccount();
    //var d= new DatiAccountBean();
    var result=ctrl.aggiornaDatiAccount(null);
    assertFalse(result.isSuccesso());
    assertEquals("Dati non validi", result.getMessaggio());
    }

    @Test
    @DisplayName("Aggiorna Dati Account: id <= 0 ->Ko")
    void aggiornaDatiAccount_idUtenteMinoreDi0_returnKo(){
    var ctrl=new LogicControllerGestioneAccount();
    var d= new DatiAccountBean();
    d.setIdUtente(0);
    var result=ctrl.aggiornaDatiAccount(d);
    assertFalse(result.isSuccesso());
    assertEquals("Dati non validi", result.getMessaggio());
    }


}
