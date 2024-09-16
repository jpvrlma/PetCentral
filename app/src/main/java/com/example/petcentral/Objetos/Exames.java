package com.example.petcentral.Objetos;

import com.google.firebase.Timestamp;

public class Exames {
    private String id;
    private String urlArquivo;
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

    public String getUrlArquivo() {
        return urlArquivo;
    }

    public void setUrlArquivo(String urlArquivo) {
        this.urlArquivo = urlArquivo;
    }

    public Exames(String anotacoes, Timestamp data, String id, String nome, String urlArquivo) {
        this.anotacoes = anotacoes;
        this.data = data;
        this.id = id;
        this.nome = nome;
        this.urlArquivo = urlArquivo;
    }
}
