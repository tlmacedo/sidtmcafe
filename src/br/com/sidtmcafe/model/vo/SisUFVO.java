package br.com.sidtmcafe.model.vo;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SisUFVO extends RecursiveTreeObject<SisUFVO> {

    IntegerProperty id, ibge_id;
    StringProperty descricao, sigla;

    public SisUFVO() {
    }

    public int getId() {
        return idProperty().get();
    }

    public IntegerProperty idProperty() {
        if (id == null) id = new SimpleIntegerProperty(-1);
        return id;
    }

    public void setId(int id) {
        idProperty().set(id);
    }

    public int getIbge_id() {
        return ibge_idProperty().get();
    }

    public IntegerProperty ibge_idProperty() {
        if (ibge_id == null) ibge_id = new SimpleIntegerProperty(-1);
        return ibge_id;
    }

    public void setIbge_id(int ibge_id) {
        ibge_idProperty().set(ibge_id);
    }

    public String getDescricao() {
        return descricaoProperty().get();
    }

    public StringProperty descricaoProperty() {
        if (descricao == null) descricao = new SimpleStringProperty("");
        return descricao;
    }

    public void setDescricao(String descricao) {
        descricaoProperty().set(descricao);
    }

    public String getSigla() {
        return siglaProperty().get();
    }

    public StringProperty siglaProperty() {
        if (sigla == null) sigla = new SimpleStringProperty("");
        return sigla;
    }

    public void setSigla(String sigla) {
        siglaProperty().set(sigla);
    }

    @Override
    public String toString() {
        return siglaProperty().get();
    }
}
