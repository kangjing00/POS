package com.findbulous.pos;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Currency extends RealmObject {

    @PrimaryKey
    private int id;
    private String name, full_name;

    private String symbol, position;
    private double rounding;
    private int decimal_places;

    private String currency_unit_label, currency_subunit_label;
    private boolean active;

    public Currency(int id, String name, String symbol, String full_name, double rounding, int decimal_places,
                    boolean active, String position, String currency_unit_label, String currency_subunit_label){
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.full_name = full_name;
        this.rounding = rounding;
        this.decimal_places = decimal_places;
        this.active = active;
        this.position = position;
        this.currency_unit_label = currency_unit_label;
        this.currency_subunit_label = currency_subunit_label;
    }

    public Currency(){
        this.id = -1;
        this.name = null;
        this.symbol = null;
        this.full_name = null;
        this.rounding = -1;
        this.decimal_places = -1;
        this.active = false;
        this.position = null;
        this.currency_unit_label = null;
        this.currency_subunit_label = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public double getRounding() {
        return rounding;
    }

    public void setRounding(double rounding) {
        this.rounding = rounding;
    }

    public int getDecimal_places() {
        return decimal_places;
    }

    public void setDecimal_places(int decimal_places) {
        this.decimal_places = decimal_places;
    }

    public String getCurrency_unit_label() {
        return currency_unit_label;
    }

    public void setCurrency_unit_label(String currency_unit_label) {
        this.currency_unit_label = currency_unit_label;
    }

    public String getCurrency_subunit_label() {
        return currency_subunit_label;
    }

    public void setCurrency_subunit_label(String currency_subunit_label) {
        this.currency_subunit_label = currency_subunit_label;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
