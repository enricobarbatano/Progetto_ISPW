package com.ispw.bean;
import java.util.Date;


public class SessioneUtenteBean {
    private String idSessione;
    private UtenteBean utente;
    private Date timeStamp;

    public SessioneUtenteBean() { /* default ctor for frameworks/serialization */ }

    public SessioneUtenteBean(String idSessione, UtenteBean utente, Date timeStamp) {
        this.idSessione = idSessione;
        this.utente = utente;
        this.timeStamp = timeStamp;
    }

    public String getIdSessione() { 
        return idSessione; 
    }
    public void setIdSessione(String idSessione) { 
        this.idSessione = idSessione; 
    }

    public UtenteBean getUtente() { 
        return utente; 
    }
    public void setUtente(UtenteBean utente) { 
        this.utente = utente;
     }

    public Date getTimeStamp() { 
        return timeStamp; 
    }
    public void setTimeStamp(Date timeStamp) { 
        this.timeStamp = timeStamp; 
    }
}
