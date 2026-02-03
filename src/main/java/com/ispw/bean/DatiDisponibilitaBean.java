package com.ispw.bean;


public class DatiDisponibilitaBean {
    private String data; private String oraInizio; private String oraFine; private float costo;
    public String getData() { return data; } public void setData(String data) { this.data = data; }
    public String getOraInizio() { return oraInizio; } public void setOraInizio(String oraInizio) { this.oraInizio = oraInizio; }
    public String getOraFine() { return oraFine; } public void setOraFine(String oraFine) { this.oraFine = oraFine; }
    public float getCosto() { return costo; } public void setCosto(float costo) { this.costo = costo; }
    @Override public String toString() { return data+" "+oraInizio+"-"+oraFine+" ("+costo+"€)"; }
}
