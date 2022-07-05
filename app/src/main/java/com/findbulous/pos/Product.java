package com.findbulous.pos;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Product extends RealmObject {

    @PrimaryKey
    private int product_id;
    private String name;
    private double list_price;
    private String display_list_price;

    //Constructor
    public Product(int product_id, String name, double list_price, String display_list_price) {
        this.product_id = product_id;
        this.name = name;
        this.list_price = list_price;
        this.display_list_price = display_list_price;
    }
    public Product(){
        product_id = -1;
        name = null;
        list_price = -1;
        display_list_price = null;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id){ this.product_id = product_id; }

    public String getDisplay_list_price() {
        return display_list_price;
    }

    public void setDisplay_list_price(String display_list_price) {
        this.display_list_price = display_list_price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getList_price() {
        return list_price;
    }

    public void setList_price(double list_price) {
        this.list_price = list_price;
    }
}
