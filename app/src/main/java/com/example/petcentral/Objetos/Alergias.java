package com.example.petcentral.Objetos;

import com.google.firebase.Timestamp;

public class Alergias {
    private String id;
    private String alergia;
    private Timestamp dataDiagnostico;
    private String anotacoes;

    public Alergias(){

    }

    public String getAlergia() {
        return alergia;
    }

    public void setAlergia(String alergia) {
        this.alergia = alergia;
    }

    public String getAnotacoes() {
        return anotacoes;
    }

    public void setAnotacoes(String anotacoes) {
        this.anotacoes = anotacoes;
    }

    public Timestamp getDataDiagnostico() {
        return dataDiagnostico;
    }

    public void setDataDiagnostico(Timestamp dataDiagnostico) {
        this.dataDiagnostico = dataDiagnostico;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Alergias(String alergia, String anotacoes, Timestamp dataDiagnostico, String id) {
        this.alergia = alergia;
        this.anotacoes = anotacoes;
        this.dataDiagnostico = dataDiagnostico;
        this.id = id;
    }
}
