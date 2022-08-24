package com.findbulous.pos;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Payment extends RealmObject {
    @PrimaryKey
    private int local_id;
    private int id;

    private double amount;
    private String display_amount;

    private Payment_Method payment_method;
    private Order order;

    public Payment(int local_id, int id, double amount, String display_amount, Payment_Method payment_method, Order order){
        this.local_id = local_id;
        this.id = id;
        this.amount = amount;
        this.display_amount = display_amount;
        this.payment_method = payment_method;
        this.order = order;
    }

    public Payment(){
        local_id = -1;
        id = -1;
        amount = 0;
        display_amount = null;
        payment_method = null;
        order = null;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Payment_Method getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(Payment_Method payment_method) {
        this.payment_method = payment_method;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getDisplay_amount() {
        return display_amount;
    }

    public void setDisplay_amount(String display_amount) {
        this.display_amount = display_amount;
    }

    public int getLocal_id() {
        return local_id;
    }

    public void setLocal_id(int local_id) {
        this.local_id = local_id;
    }
}
