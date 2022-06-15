package com.example.pos;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;

public class Order  extends RealmObject {

    @PrimaryKey
    private int order_id;
    private String datetime;
    private RealmList<Product> products;

    //Constructor
    public Order(int order_id, String datetime, RealmList<Product> products){
        this.order_id = order_id;
        this.datetime = datetime;
        this.products = products;
    }
    public Order(){
        order_id = -1;
        products = null;
        datetime = null;
    }

    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(int order_id){ this.order_id = order_id; }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public RealmList<Product> getProducts() {
        return products;
    }

    public void setProducts(RealmList<Product> products) {
        this.products = products;
    }
}
