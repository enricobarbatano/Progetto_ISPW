package com.ispw.bean;


public class EsitoDisdettaBean {
    private boolean possibile; private float penale;
    public boolean isPossibile() { return possibile; } public void setPossibile(boolean possibile) { this.possibile = possibile; }
    public float getPenale() { return penale; } public void setPenale(float penale) { this.penale = penale; }
    @Override public String toString() { return "Disdetta " + (possibile? "possibile":"non possibile") + " - penale "+penale+"€"; }
}

