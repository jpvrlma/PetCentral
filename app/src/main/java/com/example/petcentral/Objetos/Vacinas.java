package com.example.petcentral.Objetos;

import com.google.firebase.Timestamp;

public class Vacinas {
    private String id;
    private String nome;
    private String nomePet;
    private Timestamp dataAplicacao;
    private Timestamp proximaDose;
    private String descricao;

    public Vacinas() {

    }

    public Timestamp getDataAplicacao() {
        return dataAplicacao;
    }

    public void setDataAplicacao(Timestamp dataAplicacao) {
        this.dataAplicacao = dataAplicacao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNomePet() {
        return nomePet;
    }

    public void setNomePet(String nomePet) {
        this.nomePet = nomePet;
    }

    public Timestamp getProximaDose() {
        return proximaDose;
    }

    public void setProximaDose(Timestamp proximaDose) {
        this.proximaDose = proximaDose;
    }

    public Vacinas(Timestamp dataAplicacao, String descricao, String id, String nome, String nomePet, Timestamp proximaDose) {
        this.dataAplicacao = dataAplicacao;
        this.descricao = descricao;
        this.id = id;
        this.nome = nome;
        this.nomePet = nomePet;
        this.proximaDose = proximaDose;
    }
}
