package com.findbulous.pos;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Product_Tax extends RealmObject {

    @PrimaryKey
    private String product_tax_id;     //Composite primary key  [tax_id:product_tmpl_id]
    private int tax_id, product_tmpl_id;
    //type = ["fixed", "percent", "division"]
    private String name, amount_type;
    //amount = tax amount according to its type
    //actual_amount = the calculated product tax result
    private double amount, actual_amount;
    private boolean include_base_amount;    //TRUE = start calculate from the last (tax added) price_subtotal
    private String display_amount, display_actual_amount;

    private Product product;

    public Product_Tax(int tax_id, int product_tmpl_id, String name, String amount_type,
                       double amount, double actual_amount, boolean include_base_amount,
                       String display_amount, String display_actual_amount, Product product){
        this.tax_id = tax_id;
        this.product_tmpl_id = product_tmpl_id;
        this.product_tax_id = String.format("%d:%d", tax_id, product_tmpl_id);
        this.name = name;
        this.amount_type = amount_type;
        this.amount = amount;
        this.actual_amount = actual_amount;
        this.include_base_amount = include_base_amount;
        this.display_amount = display_amount;
        this.display_actual_amount = display_actual_amount;
        this.product = product;
    }

    public Product_Tax(){
        tax_id = -1;
        product_tmpl_id = -1;
        product_tax_id = null;
        name = null;
        amount_type = null;
        amount = 0.0;
        actual_amount = 0.0;
        include_base_amount = false;
        display_amount = null;
        display_actual_amount = null;
        product = null;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
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

    public String getAmount_type() {
        return amount_type;
    }

    public void setAmount_type(String amount_type) {
        this.amount_type = amount_type;
    }

    public double getActual_amount() {
        return actual_amount;
    }

    public void setActual_amount(double actual_amount) {
        this.actual_amount = actual_amount;
    }

    public boolean isInclude_base_amount() {
        return include_base_amount;
    }

    public void setInclude_base_amount(boolean include_base_amount) {
        this.include_base_amount = include_base_amount;
    }

    public String getProduct_tax_id() {
        return product_tax_id;
    }

    public void setProduct_tax_id(String product_tax_id) {
        this.product_tax_id = product_tax_id;
    }

    public int getTax_id() {
        return tax_id;
    }

    public void setTax_id(int tax_id) {
        this.tax_id = tax_id;
    }

    public int getProduct_tmpl_id() {
        return product_tmpl_id;
    }

    public void setProduct_tmpl_id(int product_tmpl_id) {
        this.product_tmpl_id = product_tmpl_id;
    }

    public String getDisplay_amount() {
        return display_amount;
    }

    public void setDisplay_amount(String display_amount) {
        this.display_amount = display_amount;
    }

    public String getDisplay_actual_amount() {
        return display_actual_amount;
    }

    public void setDisplay_actual_amount(String display_actual_amount) {
        this.display_actual_amount = display_actual_amount;
    }
}
