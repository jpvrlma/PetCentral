package com.example.petcentral.Objetos;

import com.google.firebase.Timestamp;

public class Exames {
    private String id;
    private String arquivo;
    private String nome;
    private Timestamp data;
    private String anotacoes;

    public Exames(){

    }

    public String getAnotacoes() {
        return anotacoes;
    }

    public void setAnotacoes(String anotacoes) {
        this.anotacoes = anotacoes;
    }

    public String getArquivo() {
        return arquivo;
    }

    public void setArquivo(String arquivo) {
        this.arquivo = arquivo;
    }

    public Timestamp getData() {
        return data;
    }

    public void setData(Timestamp data) {
        this.data = data;
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

    public Exames(String anotacoes, String arquivo, Timestamp data, String id, String nome) {
        this.anotacoes = anotacoes;
        this.arquivo = arquivo;
        this.data = data;
        this.id = id;
        this.nome = nome;
    }
}
