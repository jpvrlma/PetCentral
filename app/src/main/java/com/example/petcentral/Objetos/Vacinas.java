package com.example.petcentral.Objetos;

import com.google.firebase.Timestamp;

public class Vacinas {
    private String id;
    private Timestamp dataAplicacao;
    private Timestamp proximaDose;
    private String descricao;

    public Vacinas(){

    }

    public Vacinas(String id, Timestamp dataAplicacao, Timestamp proximaDose, String descricao) {
        this.id = id;
        this.dataAplicacao = dataAplicacao;
        this.proximaDose = proximaDose;
        this.descricao = descricao;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
