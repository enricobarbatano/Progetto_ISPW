package com.ispw.bean;

public class DatiAccountBean extends BaseAnagraficaBean {

    private int idUtente;
    private String telefono;
    private String indirizzo;

    public DatiAccountBean() {
        // Costruttore vuoto richiesto per bean/framework (istanziazione riflessiva).
    }

    public int getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(int idUtente) {
        this.idUtente = idUtente;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }
}
