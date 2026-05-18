package com.ispw.controller.logic;

import com.ispw.controller.logic.ctrl.LogicControllerGestioneFattura;
import com.ispw.controller.logic.ctrl.LogicControllerGestioneManutenzione;
import com.ispw.controller.logic.ctrl.LogicControllerGestioneNotifica;
import com.ispw.controller.logic.ctrl.LogicControllerGestionePagamento;
import com.ispw.controller.logic.ctrl.LogicControllerGestoreDisponibilita;
import com.ispw.controller.logic.interfaces.disponibilita.GestioneDisponibilitaDisdetta;
import com.ispw.controller.logic.interfaces.disponibilita.GestioneDisponibilitaGestioneRegole;
import com.ispw.controller.logic.interfaces.disponibilita.GestioneDisponibilitaPrenotazione;
import com.ispw.controller.logic.interfaces.fattura.GestioneFatturaPenalita;
import com.ispw.controller.logic.interfaces.fattura.GestioneFatturaPrenotazione;
import com.ispw.controller.logic.interfaces.fattura.GestioneFatturaRimborso;
import com.ispw.controller.logic.interfaces.manutenzione.GestioneManutenzioneConfiguraRegole;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaConfiguraRegole;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaDisdetta;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaGestioneAccount;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaPenalita;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaPrenotazione;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaRegistrazione;
import com.ispw.controller.logic.interfaces.pagamento.GestionePagamentoPenalita;
import com.ispw.controller.logic.interfaces.pagamento.GestionePagamentoPrenotazione;
import com.ispw.controller.logic.interfaces.pagamento.GestionePagamentoRimborso;
import com.ispw.controller.logic.interfaces.pagamento.GestionePagamentoDisdetta;

public class ServiceFactory {

    // ===================== PAGAMENTO =====================

    public static  GestionePagamentoDisdetta getPagamentoDisdettaService() {
        return new LogicControllerGestionePagamento();
    }
    public static GestionePagamentoPrenotazione getPagamentoPrenotazioneService() {
        return new LogicControllerGestionePagamento();
    }

    public static GestionePagamentoPenalita getPagamentoPenalitaService() {
        return new LogicControllerGestionePagamento();
    }

    public static GestionePagamentoRimborso getPagamentoRimborsoService() {
        return new LogicControllerGestionePagamento();
    }

    // ===================== FATTURA =====================

    public static GestioneFatturaPrenotazione getFatturaPrenotazioneService() {
        return new LogicControllerGestioneFattura();
    }

    public static GestioneFatturaPenalita getFatturaPenalitaService() {
        return new LogicControllerGestioneFattura();
    }

    public static GestioneFatturaRimborso getFatturaRimborsoService() {
        return new LogicControllerGestioneFattura();
    }

    // ===================== NOTIFICA =====================

    public static GestioneNotificaPrenotazione getNotificaPrenotazioneService() {
        return new LogicControllerGestioneNotifica();
    }

    public static GestioneNotificaDisdetta getNotificaDisdettaService() {
        return new LogicControllerGestioneNotifica();
    }

    public static GestioneNotificaRegistrazione getNotificaRegistrazioneService() {
        return new LogicControllerGestioneNotifica();
    }

    public static GestioneNotificaPenalita getNotificaPenalitaService() {
        return new LogicControllerGestioneNotifica();
    }

    public static GestioneNotificaConfiguraRegole getNotificaConfiguraRegoleService() {
        return new LogicControllerGestioneNotifica();
    }

    public static GestioneNotificaGestioneAccount getNotificaGestioneAccountService() {
        return new LogicControllerGestioneNotifica();
    }

    // ===================== DISPONIBILITA =====================

    public static GestioneDisponibilitaPrenotazione getDisponibilitaPrenotazioneService() {
        return new LogicControllerGestoreDisponibilita();
    }

    public static GestioneDisponibilitaDisdetta getDisponibilitaDisdettaService() {
        return new LogicControllerGestoreDisponibilita();
    }

    public static GestioneDisponibilitaGestioneRegole getDisponibilitaRegoleService() {
        return new LogicControllerGestoreDisponibilita();
    }

    // ===================== MANUTENZIONE =====================

    public static GestioneManutenzioneConfiguraRegole getManutenzioneService() {
        return new LogicControllerGestioneManutenzione();
    }
}