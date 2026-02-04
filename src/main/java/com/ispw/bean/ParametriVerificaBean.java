package com.ispw.bean;


public class ParametriVerificaBean {
    private int idCampo; 
    private String data; 
    private String oraInizio; 
    private int durataMin;



   public int getIdCampo() { 
    return idCampo; 
}
    public void setIdCampo(int idCampo) {
         this.idCampo = idCampo;
         }

    public String getData() 
    { 
        return data; 
    } 
    public void setData(String data)
     {
         this.data = data; 
        }
    public String getOraInizio() 
    {
         return oraInizio; 
        } 
    public void setOraInizio(String oraInizio)
     { 
        this.oraInizio = oraInizio; 
    }
    public int getDurataMin() 
    { 
        return durataMin; 
    } 
    public void setDurataMin(int durataMin)
     { 
        this.durataMin = durataMin; 
    }
}
