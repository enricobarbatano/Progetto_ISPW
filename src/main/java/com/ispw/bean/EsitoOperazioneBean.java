package com.ispw.bean;


public class EsitoOperazioneBean {
    private boolean success;
    private String messaggio;
    
    public boolean isSuccess() { 
        return success; 
    } public void setSuccess(boolean success)
     {
         this.success = success; 
        }
    public String getMessaggio() {
         return messaggio; 
        } 
    public void setMessaggio(String messaggio) 
    { 
        this.messaggio = messaggio; 
    }
    @Override 
    public String toString() {
         return (success? "OK":"KO") + (messaggio!=null? " - "+messaggio : ""); 
        }
}
