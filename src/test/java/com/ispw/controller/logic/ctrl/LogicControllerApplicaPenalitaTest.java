package com.ispw.controller.logic.ctrl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import com.ispw.bean.DatiPenalitaBean;


class LogicControllerApplicaPenalitaTest {   

   @Test 
    void applicaSanzione_datiNull_returnKO(){
        var ctrl = new LogicControllerApplicaPenalita();
        var result= ctrl.applicaSanzione(null, null, null);
        assertNotNull(result);// Verifica che il risultato non sia null
        assertFalse(result.isSuccesso());//verifica che il risultato sia un KO

    }

    @Test
    void applicaSanzione_idUtenteNonValido_returnKO(){
     var ctrl=new LogicControllerApplicaPenalita();
     var d= new DatiPenalitaBean();
        d.setIdUtente(0);
        d.setMotivazione("finta motivazione");
     var result=ctrl.applicaSanzione(d,  null, null);
        assertNotNull(result);// Verifica che il risultato non sia null
        assertFalse(result.isSuccesso());//verifica che il risultato sia un KO
    }

    @Test
    void applicaSanzione_motivazioneNonValida_returnKO(){
        var ctrl= new LogicControllerApplicaPenalita();
        var d= new DatiPenalitaBean();
        d.setMotivazione("  ");
        d.setIdUtente(1);
        var result= ctrl.applicaSanzione(d, null, null);
        assertNotNull(result);// Verifica che il risultato non sia null
        assertFalse(result.isSuccesso());//verifica che il risultato sia un KO
    }
}
