package com.ispw.controller.logic;

import com.ispw.controller.logic.ctrl.LogicControllerApplicaPenalita;
import com.ispw.controller.logic.ctrl.LogicControllerConfiguraRegole;
import com.ispw.controller.logic.ctrl.LogicControllerDisdettaPrenotazione;
import com.ispw.controller.logic.ctrl.LogicControllerGestioneAccesso;
import com.ispw.controller.logic.ctrl.LogicControllerGestioneAccount;
import com.ispw.controller.logic.ctrl.LogicControllerPrenotazioneCampo;
import com.ispw.controller.logic.ctrl.LogicControllerRegistrazione;
import com.ispw.controller.logic.interfaces.CtrlAccesso;
import com.ispw.controller.logic.interfaces.CtrlApplicaPenalita;
import com.ispw.controller.logic.interfaces.CtrlDisdetta;
import com.ispw.controller.logic.interfaces.CtrlGestioneAccount;
import com.ispw.controller.logic.interfaces.CtrlGestioneRegole;
import com.ispw.controller.logic.interfaces.CtrlPrenotazione;
import com.ispw.controller.logic.interfaces.CtrlRegistrazione;

/**
 * Factory dei controller logici principali.
 *
 * Questa classe viene usata dal layer grafico per ottenere i controller
 * applicativi dei casi d'uso senza dipendere dalle implementazioni concrete.
 *
 * Nota di progetto:
 * questa factory è distinta dalla ServiceFactory.
 * - LogicControllerFactory crea controller di caso d'uso.
 * - ServiceFactory crea controller secondari usati dai logic controller.
 */
public final class LogicControllerFactory {

    private LogicControllerFactory() {
        // Costruttore privato: questa è una utility factory e non deve essere istanziata.
    }

    public static CtrlAccesso getAccessoController() {
        return new LogicControllerGestioneAccesso();
    }

    public static CtrlGestioneAccount getGestioneAccountController() {
        return new LogicControllerGestioneAccount();
    }

    public static CtrlRegistrazione getRegistrazioneController() {
        return new LogicControllerRegistrazione();
    }

    public static CtrlPrenotazione getPrenotazioneController() {
        return new LogicControllerPrenotazioneCampo();
    }

    public static CtrlApplicaPenalita getPenalitaController() {
        return new LogicControllerApplicaPenalita();
    }

    public static CtrlGestioneRegole getGestioneRegoleController() {
        return new LogicControllerConfiguraRegole();
    }

    /*
     * Per la disdetta, se non hai ancora un'interfaccia CtrlDisdetta,
     * ritorniamo temporaneamente il controller concreto.
     *
     * Questo non cambia la logica attuale.
     * In futuro, se vorrai, potrai introdurre un'interfaccia dedicata.
     */
    public static CtrlDisdetta getDisdettaController() {
        return new LogicControllerDisdettaPrenotazione();
    }
}