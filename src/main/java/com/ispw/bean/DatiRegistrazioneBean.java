package com.ispw.bean;

public class DatiRegistrazioneBean extends BaseAnagraficaBean {

    private String password;

    public DatiRegistrazioneBean() {
        // Costruttore vuoto richiesto per bean/framework (istanziazione riflessiva).
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
