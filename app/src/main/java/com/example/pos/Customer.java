package com.example.pos;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Customer extends RealmObject {

    @PrimaryKey
    private int customer_id;
    private String customer_name, customer_email, customer_phoneNo;

    //Constructor
    public Customer(int customer_id, String customer_name, String customer_email, String customer_phoneNo){
        this.customer_id = customer_id;
        this.customer_name = customer_name;
        this.customer_email = customer_email;
        this.customer_phoneNo = customer_phoneNo;
    }
    public Customer(){
        customer_id = -1;
        customer_name = null;
        customer_email = null;
        customer_phoneNo = null;
    }

    public int getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(int customer_id){ this.customer_id = customer_id; }

    public String getCustomer_name() {
        return customer_name;
    }

    public void setCustomer_name(String customer_name) {
        this.customer_name = customer_name;
    }

    public String getCustomer_email() {
        return customer_email;
    }

    public void setCustomer_email(String customer_email) {
        this.customer_email = customer_email;
    }

    public String getCustomer_phoneNo() {
        return customer_phoneNo;
    }

    public void setCustomer_phoneNo(String customer_phoneNo) {
        this.customer_phoneNo = customer_phoneNo;
    }
}
