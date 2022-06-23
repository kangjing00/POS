package com.findbulous.pos;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Product extends RealmObject {

    @PrimaryKey
    private int product_id;
    private String product_name;
    private double product_price;

    //Constructor
    public Product(int product_id, String product_name, double product_price) {
        this.product_id = product_id;
        this.product_name = product_name;
        this.product_price = product_price;
    }
    public Product(){
        product_id = -1;
        product_name = null;
        product_price = -1;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id){ this.product_id = product_id; }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public double getProduct_price() {
        return product_price;
    }

    public void setProduct_price(double product_price) {
        this.product_price = product_price;
    }
}
