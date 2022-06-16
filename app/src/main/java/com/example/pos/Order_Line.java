package com.example.pos;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Order_Line extends RealmObject {
    @PrimaryKey
    private int order_line_id;
    private String order_line_name;
    private int qty, discount;
    private double price_subtotal, total_cost;

    private Order order;
    private Product product;

    //Constructor
    public Order_Line(int order_line_id, String order_line_name, int qty, double price_subtotal, double total_cost, int discount, Order order, Product product){
        this.order_line_id = order_line_id;
        this.order_line_name = order_line_name;
        this.qty = qty;
        this.price_subtotal = price_subtotal;
        this.total_cost = total_cost;
        this.discount = discount;
        this.order = order;
        this.product = product;
    }
    public Order_Line(){
        order_line_id = -1;
        order_line_name = null;
        qty = 0;
        price_subtotal = 0.0;
        total_cost = 0.0;
        discount = 0;
        order = null;
        product = null;
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

    public double getTotal_cost() {
        return total_cost;
    }

    public void setTotal_cost(double total_cost) {
        this.total_cost = total_cost;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
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
}
