package com.findbulous.pos;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Order_Line extends RealmObject {
    @PrimaryKey
    private int order_line_id;
    private String name;
    private int qty;
    /*
    price subtotal = the final price of all qty of this product after discount
    price subtotal incl = include tax
    price total = the final price of all qty of this product before discount
    */
    private double price_unit, price_subtotal, price_subtotal_incl, price_before_discount, discount;
    private String discount_type, display_discount; // percentage / fixed_amount

    private String display_price_unit, display_price_subtotal, display_price_subtotal_incl, display_price_before_discount;
    private String full_product_name, customer_note;

    private Order order;
    private Product product;

    //Constructor
    public Order_Line(int order_line_id, String name, int qty, double price_unit, double price_subtotal,
                      double price_subtotal_incl, double price_before_discount, String display_price_unit,
                      String display_price_subtotal, String display_price_subtotal_incl, String display_price_before_discount,
                      String full_product_name, String customer_note, String discount_type, double discount,
                      String display_discount, Order order, Product product){
        this.order_line_id = order_line_id;
        this.name = name;
        this.qty = qty;
        this.price_unit = price_unit;
        this.price_subtotal = price_subtotal;
        this.price_subtotal_incl = price_subtotal_incl;
        this.price_before_discount = price_before_discount;
        this.display_price_unit = display_price_unit;
        this.display_price_subtotal = display_price_subtotal;
        this.display_price_subtotal_incl = display_price_subtotal_incl;
        this.display_price_before_discount = display_price_before_discount;
        this.full_product_name = full_product_name;
        this.customer_note = customer_note;
        this.discount_type = discount_type;
        this.discount = discount;
        this.display_discount = display_discount;
        this.order = order;
        this.product = product;
    }
    public Order_Line(){
        order_line_id = -1;
        name = null;
        qty = 0;
        price_unit = 0.0;
        price_subtotal = 0.0;
        price_subtotal_incl = 0.0;
        price_before_discount = 0.0;
        display_price_unit = null;
        display_price_subtotal = null;
        display_price_subtotal_incl = null;
        display_price_before_discount = null;
        full_product_name = null;
        customer_note = null;
        discount_type = null;
        discount = 0.0;
        display_discount = null;
        order = null;
        product = null;
    }

    public int getOrder_line_id() {
        return order_line_id;
    }

    public void setOrder_line_id(int order_line_id) {
        this.order_line_id = order_line_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public double getPrice_subtotal() {
        return price_subtotal;
    }

    public void setPrice_subtotal(double price_subtotal) {
        this.price_subtotal = price_subtotal;
    }

    public Order getOrder() {
        return order;
    }

//    public void setOrder(Order order) {
//        this.order = order;
//    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getPrice_unit() {
        return price_unit;
    }

    public void setPrice_unit(double price_unit) {
        this.price_unit = price_unit;
    }

    public double getPrice_before_discount() {
        return price_before_discount;
    }

    public void setPrice_before_discount(double price_before_discount) {
        this.price_before_discount = price_before_discount;
    }

    public double getPrice_subtotal_incl() {
        return price_subtotal_incl;
    }

    public void setPrice_subtotal_incl(double price_subtotal_incl) {
        this.price_subtotal_incl = price_subtotal_incl;
    }

    public String getDisplay_price_unit() {
        return display_price_unit;
    }

    public void setDisplay_price_unit(String display_price_unit) {
        this.display_price_unit = display_price_unit;
    }

    public String getDisplay_price_subtotal() {
        return display_price_subtotal;
    }

    public void setDisplay_price_subtotal(String display_price_subtotal) {
        this.display_price_subtotal = display_price_subtotal;
    }

    public String getDisplay_price_subtotal_incl() {
        return display_price_subtotal_incl;
    }

    public void setDisplay_price_subtotal_incl(String display_price_subtotal_incl) {
        this.display_price_subtotal_incl = display_price_subtotal_incl;
    }

    public String getDisplay_price_before_discount() {
        return display_price_before_discount;
    }

    public void setDisplay_price_before_discount(String display_price_before_discount) {
        this.display_price_before_discount = display_price_before_discount;
    }

    public String getFull_product_name() {
        return full_product_name;
    }

    public void setFull_product_name(String full_product_name) {
        this.full_product_name = full_product_name;
    }

    public String getCustomer_note() {
        return customer_note;
    }

    public void setCustomer_note(String customer_note) {
        this.customer_note = customer_note;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public String getDiscount_type() {
        return discount_type;
    }

    public void setDiscount_type(String discount_type) {
        this.discount_type = discount_type;
    }

    public String getDisplay_discount() {
        return display_discount;
    }

    public void setDisplay_discount(String display_discount) {
        this.display_discount = display_discount;
    }
}
