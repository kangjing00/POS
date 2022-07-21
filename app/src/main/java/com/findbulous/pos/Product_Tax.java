package com.findbulous.pos;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Product_Tax extends RealmObject {

    @PrimaryKey
    private int product_tax_id;
    private String name;
    private double amount;
    private String display_amount;

    private Product product;

    public Product_Tax(int product_tax_id, String name, double amount, String display_amount, Product product){
        this.product_tax_id = product_tax_id;
        this.name = name;
        this.amount = amount;
        this.display_amount = display_amount;
        this.product = product;
    }

    public Product_Tax(){
        product_tax_id = -1;
        name = null;
        amount = -1;
        display_amount = null;
        product = null;
    }

    public int getProduct_tax_id() {
        return product_tax_id;
    }

    public void setProduct_tax_id(int product_tax_id) {
        this.product_tax_id = product_tax_id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDisplay_amount() {
        return display_amount;
    }

    public void setDisplay_amount(String display_amount) {
        this.display_amount = display_amount;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
