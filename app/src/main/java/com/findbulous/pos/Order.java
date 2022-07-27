package com.findbulous.pos;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;

public class Order  extends RealmObject {

    @PrimaryKey
    private int order_id;
    private String name, date_order, pos_reference, note, state, state_name; //state: "paid", "draft" ("OnGoing"), "onHold"
    private double amount_tax, amount_total, amount_paid, tip_amount, amount_order_discount;
    private boolean is_tipped, has_order_discount, is_percentage;
    private int customer_count, discount_percent;

    private Table table;
    private Customer customer;

    @LinkingObjects("order")
    private final RealmResults<Order_Line> order_lines = null;

    //Constructors
    //Dine-in
    public Order(int order_id, String name, String date_order, String pos_reference, String state, String state_name,
                 double amount_tax, double amount_total, double amount_paid, double tip_amount,
                 boolean is_tipped, Table table, Customer customer, String note, double amount_order_discount,
                 boolean has_order_discount, boolean is_percentage, int customer_count, int discount_percent){
        this.order_id = order_id;
        this.name = name;
        this.date_order = date_order;
        this.pos_reference = pos_reference;
        this.state = state;
        this.state_name = state_name;
        this.amount_total = amount_total;
        this.amount_paid = amount_paid;
        this.amount_tax = amount_tax;
        this.tip_amount = tip_amount;
        this.is_tipped = is_tipped;
        this.table = table;
        this.customer = customer;
        this.note = note;
        this.amount_order_discount = amount_order_discount;
        this.has_order_discount = has_order_discount;
        this.is_percentage = is_percentage;
        this.customer_count = customer_count;
        this.discount_percent = discount_percent;
    }

    public Order(){
        order_id = -1;
        name = null;
        date_order = null;
        pos_reference = null;
        state = null;
        state_name = null;
        amount_total = 0.0;
        amount_paid = 0.0;
        amount_tax = 0.0;
        tip_amount = 0.0;
        is_tipped = false;
        table = null;
        customer = null;
        note = null;
        amount_order_discount = 0.0;
        has_order_discount = false;
        is_percentage = true;
        customer_count = 0;
        discount_percent = 0;
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

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double getAmount_order_discount() {
        return amount_order_discount;
    }

    public void setAmount_order_discount(double amount_order_discount) {
        this.amount_order_discount = amount_order_discount;
    }

    public boolean isHas_order_discount() {
        return has_order_discount;
    }

    public void setHas_order_discount(boolean has_order_discount) {
        this.has_order_discount = has_order_discount;
    }

    public boolean isIs_percentage() {
        return is_percentage;
    }

    public void setIs_percentage(boolean is_percentage) {
        this.is_percentage = is_percentage;
    }

    public int getCustomer_count() {
        return customer_count;
    }

    public void setCustomer_count(int customer_count) {
        this.customer_count = customer_count;
    }

    public int getDiscount_percent() {
        return discount_percent;
    }

    public void setDiscount_percent(int discount_percent) {
        this.discount_percent = discount_percent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPos_reference() {
        return pos_reference;
    }

    public void setPos_reference(String pos_reference) {
        this.pos_reference = pos_reference;
    }

    public String getState_name() {
        return state_name;
    }

    public void setState_name(String state_name) {
        this.state_name = state_name;
    }
}
