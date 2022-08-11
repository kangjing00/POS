package com.findbulous.pos;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Order_Line extends RealmObject {
    @PrimaryKey
    private int local_order_line_id;
    private int order_line_id;
    private String name;
    private int qty;
    /*
    price subtotal = exclude tax, after discount, all qty
    price subtotal incl = include tax, after discount, all qty
    price_before_discount = include tax, before discount, all qty
    */
    private double price_unit, price_subtotal, price_subtotal_incl, price_before_discount, discount, total_cost, price_extra;
    private String discount_type, display_discount; // percentage /or/ fixed_amount

    private String display_price_unit, display_price_subtotal, display_price_subtotal_incl, display_price_before_discount,
                    display_total_cost, display_price_extra;
    private String full_product_name, customer_note;

    private Order order;
    private Product product;

    private RealmList<Attribute_Value> attribute_values;

    //Constructor
    public Order_Line(int local_order_line_id, int order_line_id, String name, int qty, double price_unit, double price_subtotal,
                      double price_subtotal_incl, double price_before_discount, String display_price_unit,
                      String display_price_subtotal, String display_price_subtotal_incl, String display_price_before_discount,
                      String full_product_name, String customer_note, String discount_type, double discount,
                      String display_discount, double total_cost, String display_total_cost, double price_extra,
                      String display_price_extra, Order order, Product product, RealmList<Attribute_Value> attribute_values){
        this.local_order_line_id = local_order_line_id;
        this.order_line_id = order_line_id;
        this.name = name;
        this.qty = qty;
        this.price_unit = price_unit;
        this.price_subtotal = price_subtotal;
        this.price_subtotal_incl = price_subtotal_incl;
        this.price_before_discount = price_before_discount;
        this.display_price_unit = display_price_unit;
        this.display_price_subtotal = display_price_subtotal;
        this.display_price_subtotal_incl = display_price_subtotal_incl;
        this.display_price_before_discount = display_price_before_discount;
        this.full_product_name = full_product_name;
        this.customer_note = customer_note;
        this.discount_type = discount_type;
        this.discount = discount;
        this.display_discount = display_discount;
        this.total_cost = total_cost;
        this.display_total_cost = display_total_cost;
        this.price_extra = price_extra;
        this.display_price_extra = display_price_extra;
        this.order = order;
        this.product = product;
        this.attribute_values = attribute_values;
    }
    public Order_Line(){
        local_order_line_id = -1;
        order_line_id = -1;
        name = null;
        qty = 0;
        price_unit = 0.0;
        price_subtotal = 0.0;
        price_subtotal_incl = 0.0;
        price_before_discount = 0.0;
        display_price_unit = null;
        display_price_subtotal = null;
        display_price_subtotal_incl = null;
        display_price_before_discount = null;
        full_product_name = null;
        customer_note = null;
        discount_type = null;
        discount = 0.0;
        display_discount = null;
        total_cost = 0.0;
        display_total_cost = null;
        price_extra = 0.0;
        display_price_extra = null;
        order = null;
        product = null;
        attribute_values = null;
    }

    public int getOrder_line_id() {
        return order_line_id;
    }

    public void setOrder_line_id(int order_line_id) {
        this.order_line_id = order_line_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public double getPrice_unit() {
        return price_unit;
    }

    public void setPrice_unit(double price_unit) {
        this.price_unit = price_unit;
    }

    public double getPrice_before_discount() {
        return price_before_discount;
    }

    public void setPrice_before_discount(double price_before_discount) {
        this.price_before_discount = price_before_discount;
    }

    public double getPrice_subtotal_incl() {
        return price_subtotal_incl;
    }

    public void setPrice_subtotal_incl(double price_subtotal_incl) {
        this.price_subtotal_incl = price_subtotal_incl;
    }

    public String getDisplay_price_unit() {
        return display_price_unit;
    }

    public void setDisplay_price_unit(String display_price_unit) {
        this.display_price_unit = display_price_unit;
    }

    public String getDisplay_price_subtotal() {
        return display_price_subtotal;
    }

    public void setDisplay_price_subtotal(String display_price_subtotal) {
        this.display_price_subtotal = display_price_subtotal;
    }

    public String getDisplay_price_subtotal_incl() {
        return display_price_subtotal_incl;
    }

    public void setDisplay_price_subtotal_incl(String display_price_subtotal_incl) {
        this.display_price_subtotal_incl = display_price_subtotal_incl;
    }

    public String getDisplay_price_before_discount() {
        return display_price_before_discount;
    }

    public void setDisplay_price_before_discount(String display_price_before_discount) {
        this.display_price_before_discount = display_price_before_discount;
    }

    public String getFull_product_name() {
        return full_product_name;
    }

    public void setFull_product_name(String full_product_name) {
        this.full_product_name = full_product_name;
    }

    public String getCustomer_note() {
        return customer_note;
    }

    public void setCustomer_note(String customer_note) {
        this.customer_note = customer_note;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public String getDiscount_type() {
        return discount_type;
    }

    public void setDiscount_type(String discount_type) {
        this.discount_type = discount_type;
    }

    public String getDisplay_discount() {
        return display_discount;
    }

    public void setDisplay_discount(String display_discount) {
        this.display_discount = display_discount;
    }

    public int getLocal_order_line_id() {
        return local_order_line_id;
    }

    public void setLocal_order_line_id(int local_order_line_id) {
        this.local_order_line_id = local_order_line_id;
    }

    public double getTotal_cost() {
        return total_cost;
    }

    public void setTotal_cost(double total_cost) {
        this.total_cost = total_cost;
    }

    public double getPrice_extra() {
        return price_extra;
    }

    public void setPrice_extra(double price_extra) {
        this.price_extra = price_extra;
    }

    public String getDisplay_total_cost() {
        return display_total_cost;
    }

    public void setDisplay_total_cost(String display_total_cost) {
        this.display_total_cost = display_total_cost;
    }

    public String getDisplay_price_extra() {
        return display_price_extra;
    }

    public void setDisplay_price_extra(String display_price_extra) {
        this.display_price_extra = display_price_extra;
    }

    public RealmList<Attribute_Value> getAttribute_values() {
        return attribute_values;
    }

    public void setAttribute_values(RealmList<Attribute_Value> attribute_values) {
        this.attribute_values = attribute_values;
    }
}
