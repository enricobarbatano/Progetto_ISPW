package com.ispw.bean;

public class DatiDisponibilitaBean extends BaseSlotBean {
    private float costo;

    public float getCosto() {
        return costo;
    }

    public void setCosto(float costo) {
        this.costo = costo;
    }

    @Override
    public String toString() {
        return getData() + " " + getOraInizio() + "-" + getOraFine() + " (" + costo + "€)";
    }
}
