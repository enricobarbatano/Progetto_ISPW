package com.ispw.bean;


public class RiepilogoPrenotazioneBean {
    private int idPrenotazione;
    private float importoTotale; 
    private Object datiFiscali; 
    private UtenteBean utente;

    public int getIdPrenotazione() {
         return idPrenotazione; 
        }
    public void setIdPrenotazione(int idPrenotazione) { 
        this.idPrenotazione = idPrenotazione; 
    }
    public float getImportoTotale() {
         return importoTotale; 
        } 
    public void setImportoTotale(float importoTotale) { 
        this.importoTotale = importoTotale;
     }
    public Object getDatiFiscali() { 
        return datiFiscali; 
    } 
    public void setDatiFiscali(Object datiFiscali) { 
        this.datiFiscali = datiFiscali; 
    }
    public UtenteBean getUtente() { 
        return utente; 
    } 
    public void setUtente(UtenteBean utente) { 
        this.utente = utente; 
    }
    @Override 
    public String toString() { 
        return "Prenotazione #" + idPrenotazione + " - Importo " + importoTotale + "€"; 
    }
}
