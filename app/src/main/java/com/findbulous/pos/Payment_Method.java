package com.findbulous.pos;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Payment_Method extends RealmObject {

    @PrimaryKey
    private int id;
    private String name;
    private boolean is_cash_count, split_transactions, active;
    private int payment_method_id;


    public Payment_Method(int id, String name, boolean is_cash_count, boolean split_transactions,
                          boolean active, int payment_method_id){
        this.id = id;
        this.name = name;
        this.is_cash_count = is_cash_count;
        this.split_transactions = split_transactions;
        this.active = active;
        this.payment_method_id = payment_method_id;
    }

    public Payment_Method(){
        this.id = -1;
        this.name = null;
        this.is_cash_count = false;
        this.split_transactions = false;
        this.active = false;
        this.payment_method_id = -1;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isIs_cash_count() {
        return is_cash_count;
    }

    public void setIs_cash_count(boolean is_cash_count) {
        this.is_cash_count = is_cash_count;
    }

    public boolean isSplit_transactions() {
        return split_transactions;
    }

    public void setSplit_transactions(boolean split_transactions) {
        this.split_transactions = split_transactions;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getPayment_method_id() {
        return payment_method_id;
    }

    public void setPayment_method_id(int payment_method_id) {
        this.payment_method_id = payment_method_id;
    }
}
