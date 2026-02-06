package com.ispw.bean;

import java.util.ArrayList;
import java.util.List;

public class UtentiBean {

    private List<UtenteSelezioneBean> utenti = new ArrayList<>();

    public UtentiBean() {
        // costruttore di default
    }

    public List<UtenteSelezioneBean> getUtenti() {
        return utenti;
    }

    public void setUtenti(List<UtenteSelezioneBean> utenti) {
        this.utenti = (utenti == null) ? new ArrayList<>() : utenti;
    }
}
