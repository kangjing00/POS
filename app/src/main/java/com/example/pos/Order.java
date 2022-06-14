package com.example.pos;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

public class Order  extends RealmObject {

    @PrimaryKey
    private int order_id;
    private String datetime;
//    @LinkingObjects("Product")
//    private final RealmResults<Product> products;

    //Constructor
    public Order(int order_id, String datetime, RealmResults<Product> products){
        this.order_id = order_id;
        this.datetime = datetime;
        //this.products = products;
    }
    public Order(){
        order_id = -1;
        //products = null;
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

//    public RealmResults<Product> getProducts() {
//        return products;
//    }
//
//    public void setProducts(RealmResults<Product> products) {
//        this.products = products;
//    }
}
