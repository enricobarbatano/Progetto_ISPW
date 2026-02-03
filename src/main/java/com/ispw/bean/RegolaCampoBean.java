package com.ispw.bean;



public class RegolaCampoBean  {
    

    private int idCampo;
    private Boolean attivo;
    private Boolean flagManutenzione;

    public RegolaCampoBean() {}

    public int getIdCampo() { return idCampo; }
    public void setIdCampo(int idCampo) { this.idCampo = idCampo; }

    public Boolean getAttivo() { return attivo; }
    public void setAttivo(Boolean attivo) { this.attivo = attivo; }

    public Boolean getFlagManutenzione() { return flagManutenzione; }
    public void setFlagManutenzione(Boolean flagManutenzione) { this.flagManutenzione = flagManutenzione; }
}
