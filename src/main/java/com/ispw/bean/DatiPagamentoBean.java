package com.ispw.bean;


public class DatiPagamentoBean {
    private String metodo; 
    private String credenziale; 
    private float importo;
    
    public String getMetodo() { 
        return metodo; 
    } 
    public void setMetodo(String metodo) { 
        this.metodo = metodo; 
    }
    public String getCredenziale() { 
        return credenziale; 
    } 
    public void setCredenziale(String credenziale) { 
        this.credenziale = credenziale; 
    }
    public float getImporto() { 
        return importo; 
    } 
    public void setImporto(float importo) {
         this.importo = importo; 
        }
}
