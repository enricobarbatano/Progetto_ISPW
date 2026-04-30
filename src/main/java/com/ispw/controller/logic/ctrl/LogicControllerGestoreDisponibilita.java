package com.ispw.controller.logic.ctrl;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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
     * - recupera prenotazione
     * - recupera campo
     * - sblocca slot
     * - salva campo
     * Best-effort: non solleva eccezioni al chiamante.
     */
    @Override
    public void liberaSlot(int idPrenotazione) {
        if (idPrenotazione <= 0) return;

        Prenotazione p;
        try {
            p = prenotazioneDAO().load(idPrenotazione);
        } catch (RuntimeException ex) {
            return; // best-effort
        }
        if (p == null) return;

        if (p.getData() == null || p.getOraInizio() == null) return;

        Campo c = null;
        try {
            c = (p.getCampo() != null) ? p.getCampo() : campoDAO().findById(p.getIdCampo());
        } catch (RuntimeException ex) {
            return; // best-effort
        }
        if (c == null) return;

        try {
            c.sbloccaSlot(Date.valueOf(p.getData()), Time.valueOf(p.getOraInizio()));
            campoDAO().store(c);
        } catch (RuntimeException ex) {
            // best-effort: non propagare
        }
    }

    /**
     * Rimuove la disponibilità del campo (lo rende non prenotabile).
     * Ritorna TRUE se operazione riuscita.
     */
    @Override
    public Boolean rimuoviDisponibilita(int idCampo) {
        if (idCampo <= 0) return false;

        Campo c;
        try {
            c = campoDAO().findById(idCampo);
        } catch (RuntimeException ex) {
            return false;
        }
        if (c == null) return false;

        try {
            c.updateStatoOperativo(false, c.isFlagManutenzione());
            campoDAO().store(c);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Attiva la disponibilità del campo (prenotabile).
     * Non avendo parametri temporali, restituiamo lista vuota (snello).
     */
    @Override
    public List<DatiDisponibilitaBean> attivaDisponibilita(int idCampo) {
        if (idCampo <= 0) return List.of();

        Campo c;
        try {
            c = campoDAO().findById(idCampo);
        } catch (RuntimeException ex) {
            return List.of();
        }
        if (c == null) return List.of();

        try {
            c.updateStatoOperativo(true, c.isFlagManutenzione());
            campoDAO().store(c);
            return List.of();
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    /**
     * Verifica disponibilità:
     * - Se idCampo presente, verifica solo quel campo, altrimenti su tutti i campi;
     * - Calcola oraFine = oraInizio + durataMin (default 60 se <=0);
     * - Usa Campo.isDisponibile(Date, Time, Time) e stato operativo;
     * - Costruisce DatiDisponibilitaBean.
     */
    @Override
    public List<DatiDisponibilitaBean> verificaDisponibilita(ParametriVerificaBean param) {
        if (param == null) return List.of();

        final String sData = param.getData();
        final String sOra  = param.getOraInizio();
        if (sData == null || sOra == null) return List.of();

        final LocalDate data;
        final LocalTime oraInizio;
        try {
            data = LocalDate.parse(sData.trim());
            oraInizio = LocalTime.parse(sOra.trim());
        } catch (RuntimeException ex) {
            return List.of();
        }

        int durata = (param.getDurataMin() <= 0) ? 60 : param.getDurataMin();
        LocalTime oraFine = oraInizio.plusMinutes(durata);

        List<Campo> campi;
        CampoDAO cDAO = campoDAO();
        try {
            if (param.getIdCampo() > 0) {
                Campo unico = cDAO.findById(param.getIdCampo());
                campi = (unico != null) ? List.of(unico) : List.of();
            } else {
                campi = cDAO.findAll();
            }
        } catch (RuntimeException ex) {
            return List.of();
        }

        List<DatiDisponibilitaBean> out = new ArrayList<>();
        Date sqlDate = Date.valueOf(data);
        Time sqlInizio = Time.valueOf(oraInizio);
        Time sqlFine   = Time.valueOf(oraFine);

        for (Campo c : campi) {
            if (c == null) continue;

            boolean ok;
            try {
                ok = c.isDisponibile(sqlDate, sqlInizio, sqlFine)
                  && c.isAttivo()
                  && !c.isFlagManutenzione();
            } catch (RuntimeException ex) {
                ok = false;
            }

            if (ok) {
                DatiDisponibilitaBean bean = new DatiDisponibilitaBean();
                bean.setData(data.toString());
                bean.setOraInizio(oraInizio.toString());
                bean.setOraFine(oraFine.toString());

                Float costoOrarioObj = c.getCostoOrario();
                float costoOrario = (costoOrarioObj != null) ? costoOrarioObj : 0f;
                bean.setCosto(costoOrario * (durata / 60f));

                out.add(bean);
            }
        }
        return out;
    }
}
