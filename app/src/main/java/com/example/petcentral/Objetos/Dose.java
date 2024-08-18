package com.example.petcentral.Objetos;

import com.google.firebase.Timestamp;

public class Dose {
    private String id;
    private Timestamp dataAplicacao;
    private Timestamp proximaDose;
    private String anotações;

    public Dose() {
    }

    public Dose(String id, Timestamp dataAplicacao, Timestamp proximaDose, String anotações) {
        this.id = id;
        this.dataAplicacao = dataAplicacao;
        this.proximaDose = proximaDose;
        this.anotações = anotações;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getDataAplicacao() {
        return dataAplicacao;
    }

    public void setDataAplicacao(Timestamp dataAplicacao) {
        this.dataAplicacao = dataAplicacao;
    }

    public Timestamp getProximaDose() {
        return proximaDose;
    }

    public void setProximaDose(Timestamp proximaDose) {
        this.proximaDose = proximaDose;
    }

    public String getAnotações() {
        return anotações;
    }

    public void setAnotações(String anotações) {
        this.anotações = anotações;
    }
}

