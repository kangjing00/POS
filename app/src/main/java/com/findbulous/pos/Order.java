package com.findbulous.pos;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;

public class Order  extends RealmObject {

    @PrimaryKey
    private int order_id;
    private String date_order, state; //State: "paid", "draft", "onHold"
    private double amount_total, amount_paid, amount_tax, tip_amount;
    private boolean is_tipped;
    @LinkingObjects("order")
    private final RealmResults<Order_Line> order_lines = null;

    private Table table;

    //Constructors
    public Order(int order_id, String date_order, String state, double amount_total, double amount_paid, double amount_tax, double tip_amount, boolean is_tipped, Table table){
        this.order_id = order_id;
        this.date_order = date_order;
        this.state = state;
        this.amount_total = amount_total;
        this.amount_paid = amount_paid;
        this.amount_tax = amount_tax;
        this.tip_amount = tip_amount;
        this.is_tipped = is_tipped;
        this.table = table;
    }
    public Order(int order_id, String date_order, String state, double amount_total, double amount_paid, double amount_tax, double tip_amount, boolean is_tipped){
        this.order_id = order_id;
        this.date_order = date_order;
        this.state = state;
        this.amount_total = amount_total;
        this.amount_paid = amount_paid;
        this.amount_tax = amount_tax;
        this.tip_amount = tip_amount;
        this.is_tipped = is_tipped;
        this.table = null;
    }
    public Order(){
        order_id = -1;
        date_order = null;
        state = null;
        amount_total = 0.0;
        amount_paid = 0.0;
        amount_tax = 0.0;
        tip_amount = 0.0;
        is_tipped = false;
        this.table = null;
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

    public double getAmount_tax() {
        return amount_tax;
    }

    public void setAmount_tax(double amount_tax) {
        this.amount_tax = amount_tax;
    }

    public double getTip_amount() {
        return tip_amount;
    }

    public void setTip_amount(double tip_amount) {
        this.tip_amount = tip_amount;
    }

    public boolean isIs_tipped() {
        return is_tipped;
    }

    public void setIs_tipped(boolean is_tipped) {
        this.is_tipped = is_tipped;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

//    public void setOrder_lines(){
//        this.order_lines = order_lines;
//    }
}
