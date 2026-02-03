package com.ispw.controller.logic.ctrl;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.ispw.bean.DatiDisponibilitaBean;
import com.ispw.bean.ParametriVerificaBean;
import com.ispw.controller.logic.interfaces.disponibilita.GestioneDisponibilitaDisdetta;
import com.ispw.controller.logic.interfaces.disponibilita.GestioneDisponibilitaGestioneRegole;
import com.ispw.controller.logic.interfaces.disponibilita.GestioneDisponibilitaPrenotazione;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.CampoDAO;
import com.ispw.dao.interfaces.PrenotazioneDAO;
import com.ispw.model.entity.Campo;
import com.ispw.model.entity.Prenotazione;

public class LogicControllerGestoreDisponibilita
        implements GestioneDisponibilitaDisdetta,
                   GestioneDisponibilitaGestioneRegole,
                   GestioneDisponibilitaPrenotazione {

   
    private CampoDAO campoDAO() {
        return DAOFactory.getInstance().getCampoDAO();
    }
    private PrenotazioneDAO prenotazioneDAO() {
        return DAOFactory.getInstance().getPrenotazioneDAO();
    }


    /**
     * Libera lo slot occupato da una prenotazione:
     * - recupera la prenotazione;
     * - recupera/ottiene il Campo associato (diretto o via idCampo);
     * - invoca Campo.sbloccaSlot(data, oraInizio);
     * - persiste il Campo.
     * Non cambia lo stato della prenotazione (delegalo al caso d’uso Disdetta).
     */
    @Override
    public void liberaSlot(int idPrenotazione) {
        Prenotazione p = prenotazioneDAO().load(idPrenotazione);
        if (p == null) return;

        Campo c = (p.getCampo() != null) ? p.getCampo() : campoDAO().findById(p.getIdCampo());
        if (c == null || p.getData() == null || p.getOraInizio() == null) return;

        c.sbloccaSlot(Date.valueOf(p.getData()), Time.valueOf(p.getOraInizio()));
        campoDAO().store(c);
    }

    /**
     * Rimuove la disponibilità del campo (lo rende non prenotabile).
     * Ritorna TRUE se l’operazione va a buon fine.
     */
    @Override
    public Boolean rimuoviDisponibilità(int idCampo) {
        Campo c = campoDAO().findById(idCampo);
        if (c == null) return false;

        try {
            c.updateStatoOperativo(c.getIdCampo(), /*isAttivo*/ false, c.isFlagManutenzione());
            campoDAO().store(c);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Attiva la disponibilità del campo (prenotabile).
     * Non avendo parametri temporali, restituiamo lista vuota (snello).
     */
    @Override
    public List<DatiDisponibilitaBean> attivaDisponibilità(int idCampo) {
        Campo c = campoDAO().findById(idCampo);
        if (c == null) return List.of();

        try {
            c.updateStatoOperativo(c.getIdCampo(), /*isAttivo*/ true, c.isFlagManutenzione());
            campoDAO().store(c);
            return List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Verifica disponibilità:
     * - Se idCampo è presente, verifica solo quel campo, altrimenti su tutti i campi;
     * - Calcola oraFine = oraInizio + durataMin (default 60 se null);
     * - Usa Campo.isDisponibile(Date, Time, Time) e lo stato operativo;
     * - Costruisce DatiDisponibilitaBean con data/ore in stringa e costo pro-rata.
     */
    @Override
    public List<DatiDisponibilitaBean> verificaDisponibilita(ParametriVerificaBean param) {
        Objects.requireNonNull(param, "ParametriVerificaBean non può essere null");

        // Parse dei parametri STRING in tipi temporali
        LocalDate data = LocalDate.parse(param.getData());          // yyyy-MM-dd
        LocalTime oraInizio = LocalTime.parse(param.getOraInizio()); // HH:mm
        int durata = (param.getDurataMin() <=0 ) ? param.getDurataMin() : 60;
        LocalTime oraFine = oraInizio.plusMinutes(durata);

        List<Campo> campi;
        CampoDAO cDAO = campoDAO();
        if (param.getIdCampo() <0) {
            Campo unico = cDAO.findById(param.getIdCampo());
            campi = (unico != null) ? List.of(unico) : List.of();
        } else {
            campi = cDAO.findAll();
        }

        List<DatiDisponibilitaBean> out = new ArrayList<>();
        Date sqlDate = Date.valueOf(data);
        Time sqlInizio = Time.valueOf(oraInizio);
        Time sqlFine   = Time.valueOf(oraFine);

        for (Campo c : campi) {
            boolean ok = c.isDisponibile(sqlDate, sqlInizio, sqlFine)
                      && c.isAttivo()
                      && !c.isFlagManutenzione();

            if (ok) {
                DatiDisponibilitaBean bean = new DatiDisponibilitaBean();
                bean.setData(param.getData());
                bean.setOraInizio(param.getOraInizio());
                bean.setOraFine(oraFine.toString());
                // costo pro-rata (costoOrario * durata/60). Gestiamo null-safety.
                float costoOrario = (c.getCostoOrario() != null) ? c.getCostoOrario() : 0f;
                bean.setCosto(costoOrario * (durata / 60f));

                out.add(bean);
            }
        }
        return out;
    }

    /* ==========================================
       Altri metodi (Disdetta/Regole) in futuro.
       ========================================== */
}