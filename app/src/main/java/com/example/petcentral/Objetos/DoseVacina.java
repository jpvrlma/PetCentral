package com.example.petcentral.Objetos;

import com.google.firebase.Timestamp;

public class DoseVacina extends Dose{

    private String marca;
    private String lote;

    public DoseVacina() {
    }

    public DoseVacina(String marca, String lote) {
        this.marca = marca;
        this.lote = lote;
    }

    public DoseVacina(String id, Timestamp dataAplicacao, Timestamp proximaDose, String anotações, String marca, String lote) {
        super(id, dataAplicacao, proximaDose, anotações);
        this.marca = marca;
        this.lote = lote;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }
}
