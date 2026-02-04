package com.ispw.bean;

import java.math.BigDecimal;

public class CampoBean  {

    private int idCampo;
    private String nome;
    private String tipoSport;
    private BigDecimal costoOrario;
    private boolean attivo;
    private boolean flagManutenzione;

    
     
    
    @SuppressWarnings("java:S1186") 
    public CampoBean() {
         //Nota: costruttore no-args intenzionalmente vuoto.
    }

    public int getIdCampo() { 
        return idCampo; }
    public void setIdCampo(int idCampo) { 
        this.idCampo = idCampo;
     }
    public String getNome() { 
        return nome; 
    }
    public void setNome(String nome) {
         this.nome = nome; 
        }
    public String getTipoSport() {
         return tipoSport;
         }
    public void setTipoSport(String tipoSport) { 
        this.tipoSport = tipoSport; 
    }
    public BigDecimal getCostoOrario() { 
        return costoOrario; 
    }
    public void setCostoOrario(BigDecimal costoOrario) { 
        this.costoOrario = costoOrario; 
    }
    public boolean isAttivo() {
         return attivo;
         }
    public void setAttivo(boolean attivo) {
         this.attivo = attivo; 
        }
    public boolean isFlagManutenzione() { 
        return flagManutenzione; 
    }
    public void setFlagManutenzione(boolean flagManutenzione) {
         this.flagManutenzione = flagManutenzione; 
        }
}