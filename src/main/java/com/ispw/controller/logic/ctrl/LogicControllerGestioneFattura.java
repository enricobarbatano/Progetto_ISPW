package com.ispw.controller.logic.ctrl;

import com.ispw.bean.DatiFatturaBean;
import com.ispw.controller.logic.interfaces.fattura.GestioneFatturaPenalita;
import com.ispw.controller.logic.interfaces.fattura.GestioneFatturaPrenotazione;
import com.ispw.controller.logic.interfaces.fattura.GestioneFatturaRimborso;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.FatturaDAO;
import com.ispw.model.entity.Fattura;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;


public class LogicControllerGestioneFattura
        implements GestioneFatturaPrenotazione, GestioneFatturaPenalita, GestioneFatturaRimborso {

  
    //Genera una fattura per una prenotazione (validazione essenziale + store via DAO). */
    @Override
    public Fattura generaFatturaPrenotazione(DatiFatturaBean dati, int idPrenotazione) {
        if (!isValidDati(dati)) {
            log().warning("[FATTURA][WARN] DatiFatturaBean non validi per prenotazione");
            return null;
        }
        if (idPrenotazione <= 0) {
            log().log(Level.WARNING, "[FATTURA][WARN] idPrenotazione non valido: {0}", idPrenotazione);
            return null;
        }

        final LocalDate emissione = (dati.getDataOperazione() != null) ? dati.getDataOperazione() : LocalDate.now();

        Fattura f = new Fattura();
        f.setIdPrenotazione(idPrenotazione);
        f.setCodiceFiscaleCliente(trimOrNull(dati.getCodiceFiscaleCliente()));
        f.setDataEmissione(emissione);
        f.setLinkPdf(buildPdfLink("FATT", idPrenotazione, emissione));

        fatturaDAO().store(f); // upsert delegato al DAO concreto
        log().log(Level.INFO, "[FATTURA] Emessa fattura prenotazione #{0} -> {1}",
                new Object[]{idPrenotazione, f.getLinkPdf()});
        return f;
    }

    // ===============================================================
    //  FATTURA PENALITÀ
    // ===============================================================

    /**
     * Genera una fattura per penalità (convenzione: ref prenotazione = -idPenalita).
     * NB: nome del metodo SENZA accento, come nell’interfaccia.
     */
    @Override
    public Fattura generaFatturaPenalita(DatiFatturaBean dati, int idPenalita) {
        if (!isValidDati(dati)) {
            log().warning("[FATTURA][WARN] DatiFatturaBean non validi per penalita");
            return null;
        }
        if (idPenalita <= 0) {
            log().log(Level.WARNING, "[FATTURA][WARN] idPenalita non valido: {0}", idPenalita);
            return null;
        }

        final int idPrenotazioneFittizio = -Math.abs(idPenalita);
        final LocalDate emissione = (dati.getDataOperazione() != null) ? dati.getDataOperazione() : LocalDate.now();

        Fattura f = new Fattura();
        f.setIdPrenotazione(idPrenotazioneFittizio);
        f.setCodiceFiscaleCliente(trimOrNull(dati.getCodiceFiscaleCliente()));
        f.setDataEmissione(emissione);
        f.setLinkPdf(buildPdfLink("PEN", idPrenotazioneFittizio, emissione));

        fatturaDAO().store(f);
        log().log(Level.INFO, "[FATTURA] Emessa fattura penalita #{0} (ref {1}) -> {2}",
                new Object[]{idPenalita, idPrenotazioneFittizio, f.getLinkPdf()});
        return f;
    }

    // ===============================================================
    //  NOTA DI CREDITO (RIMBORSO)
    // ===============================================================

    /** Emette una nota di credito per la prenotazione indicata (id int, come da interfaccia). */
    @Override
    public void emettiNotaDiCredito(int idPrenotazione) {
        if (idPrenotazione <= 0) {
            log().log(Level.WARNING, "[FATTURA][WARN] idPrenotazione non valido per NC: {0}", idPrenotazione);
            return;
        }

        final LocalDate oggi = LocalDate.now();
        Fattura nc = new Fattura();
        nc.setIdPrenotazione(idPrenotazione);
        // Il CF può essere popolato da un orchestratore principale, se necessario.
        nc.setDataEmissione(oggi);
        nc.setLinkPdf(buildPdfLink("NC", idPrenotazione, oggi));

        fatturaDAO().store(nc);
        log().log(Level.INFO, "[FATTURA] Emetti NC prenotazione #{0} -> {1}",
                new Object[]{idPrenotazione, nc.getLinkPdf()});
    }

    // ===============================================================
    //  Helper privati (stateless, SonarCloud friendly)
    // ===============================================================

    /** Validazione "scolastica": bean non nullo e CF presente. */
    private boolean isValidDati(DatiFatturaBean dati) {
        return dati != null && hasText(dati.getCodiceFiscaleCliente());
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        final String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /** Link PDF fittizio e deterministico (evita duplicazioni di stringhe in linea). */
    private String buildPdfLink(String prefix, int ref, LocalDate date) {
        final String d = date.format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd
        return prefix + "-" + Math.abs(ref) + "-" + d + ".pdf";
    }

    /** DAO on-demand (nessun campo, nessuna dipendenza da concreti). */
    private FatturaDAO fatturaDAO() {
        return DAOFactory.getInstance().getFatturaDAO();
    }

    /** Logger on-demand per evitare campi (stateless). S1312 soppressa localmente. */
    @SuppressWarnings("java:S1312")
    private Logger log() {
        return Logger.getLogger(getClass().getName());
    }
}