package com.ispw.bean;



import java.time.LocalTime;

public class TempisticheBean {

    private int durataSlotMinuti;
    private LocalTime oraApertura;
    private LocalTime oraChiusura;
    private int preavvisoMinimoMinuti;

    public TempisticheBean() {
        //Nota: costruttore no-args intenzionalmente vuoto.
    }

    public int getDurataSlotMinuti() {
         return durataSlotMinuti; 
        }
    public void setDurataSlotMinuti(int durataSlotMinuti) { 
        this.durataSlotMinuti = durataSlotMinuti; 
    }

    public LocalTime getOraApertura() { 
        return oraApertura; 
    }
    public void setOraApertura(LocalTime oraApertura) { 
        this.oraApertura = oraApertura; 
    }

    public LocalTime getOraChiusura() { 
        return oraChiusura;
     }
    public void setOraChiusura(LocalTime oraChiusura) {
         this.oraChiusura = oraChiusura; 
        }

    public int getPreavvisoMinimoMinuti() {
         return preavvisoMinimoMinuti; 
        }
    public void setPreavvisoMinimoMinuti(int preavvisoMinimoMinuti) { 
        this.preavvisoMinimoMinuti = preavvisoMinimoMinuti;
     }
}
