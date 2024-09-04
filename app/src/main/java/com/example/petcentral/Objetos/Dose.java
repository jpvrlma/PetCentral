package com.example.petcentral.Objetos;

import com.google.firebase.Timestamp;

public class Dose {
    private String id;
    private Timestamp dataAplicacao;
    private Timestamp proximaDose;
    private String anotacoes;
    private int numeroDose;

    public Dose() {
    }

    public Dose(String id, Timestamp dataAplicacao, Timestamp proximaDose, String anotacoes,int numeroDose) {
        this.id = id;
        this.dataAplicacao = dataAplicacao;
        this.proximaDose = proximaDose;
        this.anotacoes = anotacoes;
        this.numeroDose = numeroDose;
    }

    public String getAnotacoes() {
        return anotacoes;
    }

    public void setAnotacoes(String anotacoes) {
        this.anotacoes = anotacoes;
    }

    public Timestamp getDataAplicacao() {
        return dataAplicacao;
    }

    public void setDataAplicacao(Timestamp dataAplicacao) {
        this.dataAplicacao = dataAplicacao;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumeroDose() {
        return numeroDose;
    }

    public void setNumeroDose(int numeroDose) {
        this.numeroDose = numeroDose;
    }

    public Timestamp getProximaDose() {
        return proximaDose;
    }

    public void setProximaDose(Timestamp proximaDose) {
        this.proximaDose = proximaDose;
    }
}

