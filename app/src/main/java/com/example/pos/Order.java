package com.example.pos;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;

public class Order  extends RealmObject {

    @PrimaryKey
    private int order_id;
    private String date_order, state; //State: "paid", "draft"
    private double amount_total, amount_paid;
    @LinkingObjects("order")
    private final RealmResults<Order_Line> order_lines;

    //Constructor
    public Order(int order_id, String date_order, String state, RealmResults<Order_Line> order_lines, double amount_total, double amount_paid){
        this.order_id = order_id;
        this.date_order = date_order;
        this.state = state;
        this.order_lines = order_lines;
        this.amount_total = amount_total;
        this.amount_paid = amount_paid;
    }
    public Order(){
        order_id = -1;
        date_order = null;
        state = null;
        order_lines = null;
        amount_total = 0.0;
        amount_paid = 0.0;
    }

    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(int order_id){ this.order_id = order_id; }

    public String getDate_order() {
        return date_order;
    }

    public void setDate_order(String date_order) {
        this.date_order = date_order;
    }

    public RealmResults<Order_Line> getOrder_lines() {
        return order_lines;
    }

    public double getAmount_total() {
        return amount_total;
    }

    public void setAmount_total(double amount_total) {
        this.amount_total = amount_total;
    }

    public double getAmount_paid() {
        return amount_paid;
    }

    public void setAmount_paid(double amount_paid) {
        this.amount_paid = amount_paid;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

//    public void setOrder_lines(){
//        this.order_lines = order_lines;
//    }
}
