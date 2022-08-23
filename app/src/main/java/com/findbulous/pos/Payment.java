package com.findbulous.pos;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Payment extends RealmObject {
    @PrimaryKey
    private int id;

    private double amount;

    private Payment_Method payment_method;
    private Order order;

    public Payment(int id, double amount, Payment_Method payment_method, Order order){
        this.id = id;
        this.amount = amount;
        this.payment_method = payment_method;
        this.order = order;
    }

    public Payment(){
        id = -1;
        amount = 0;
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
}
