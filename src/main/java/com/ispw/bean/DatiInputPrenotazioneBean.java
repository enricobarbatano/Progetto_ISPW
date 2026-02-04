
package com.ispw.bean;

public class DatiInputPrenotazioneBean {
    private int idCampo;         
    private String data;         
    private String oraInizio;    
    private String oraFine;      
    
    public int getIdCampo() { 
        return idCampo; 
    }
    public void setIdCampo(int idCampo) {
         this.idCampo = idCampo;
         }

    public String getData() { 
        return data; 
    }
    public void setData(String data) {
         this.data = data; 
        }

    public String getOraInizio() { 
        return oraInizio; 
    }
    public void setOraInizio(String oraInizio) { 
        this.oraInizio = oraInizio; 
    }

    public String getOraFine() { 
        return oraFine; 
    }
    public void setOraFine(String oraFine) {
         this.oraFine = oraFine; 
        }
}
