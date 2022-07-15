package com.findbulous.pos;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Order_Line extends RealmObject {
    @PrimaryKey
    private int order_line_id;
    private String order_line_name;
    private int qty, discount_percent;
    //price subtotal = the final price of all qty of this product after discount
    //price total = the final price of all qty of this product before discount;
    private double product_price, price_subtotal, price_total, amount_discount;
    private boolean has_discount, is_percentage;

    private Order order;
    private Product product;

    //Constructor
    public Order_Line(int order_line_id, String order_line_name, int qty, double product_price, double price_subtotal,
                      double price_total, int discount_percent, Order order, Product product,
                      boolean has_discount, boolean is_percentage, double amount_discount){
        this.order_line_id = order_line_id;
        this.order_line_name = order_line_name;
        this.qty = qty;
        this.product_price = product_price;
        this.price_subtotal = price_subtotal;
        this.price_total = price_total;
        this.discount_percent = discount_percent;
        this.order = order;
        this.product = product;
        this.has_discount = has_discount;
        this.is_percentage = is_percentage;
        this.amount_discount = amount_discount;
    }
    public Order_Line(){
        order_line_id = -1;
        order_line_name = null;
        qty = 0;
        product_price = 0.0;
        price_subtotal = 0.0;
        price_total = 0.0;
        discount_percent = 0;
        order = null;
        product = null;
        has_discount = false;
        is_percentage = true;
        amount_discount = 0.0;
    }

    public int getOrder_line_id() {
        return order_line_id;
    }

    public void setOrder_line_id(int order_line_id) {
        this.order_line_id = order_line_id;
    }

    public String getOrder_line_name() {
        return order_line_name;
    }

    public void setOrder_line_name(String order_line_name) {
        this.order_line_name = order_line_name;
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

    public double getPrice_total() {
        return price_total;
    }

    public void setPrice_total(double price_total) {
        this.price_total = price_total;
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

    public int getDiscount_percent() {
        return discount_percent;
    }

    public void setDiscount_percent(int discount_percent) {
        this.discount_percent = discount_percent;
    }

    public boolean isHas_discount() {
        return has_discount;
    }

    public void setHas_discount(boolean has_discount) {
        this.has_discount = has_discount;
    }

    public boolean isIs_percentage() {
        return is_percentage;
    }

    public void setIs_percentage(boolean is_percentage) {
        this.is_percentage = is_percentage;
    }

    public double getAmount_discount() {
        return amount_discount;
    }

    public void setAmount_discount(double amount_discount) {
        this.amount_discount = amount_discount;
    }

    public double getProduct_price() {
        return product_price;
    }

    public void setProduct_price(double product_price) {
        this.product_price = product_price;
    }
}
