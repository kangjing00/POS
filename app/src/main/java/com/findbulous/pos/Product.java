package com.findbulous.pos;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;

public class Product extends RealmObject {

    @PrimaryKey
    private int id;
    private int product_id, product_tmpl_id;
    private String name, default_code;
    private double list_price, standard_price, margin, margin_percent,
            price_incl_tax, price_excl_tax;
    private String display_list_price, display_standard_price, display_margin, display_margin_percent,
            display_price_incl_tax, display_price_excl_tax;

    private POS_Category category;

    @LinkingObjects("product")
    private final RealmResults<Product_Tax> product_taxes = null;

    //Constructor
    public Product(int id, int product_id, int product_tmpl_id, String name, String default_code,
                   double list_price, double standard_price, double margin, double margin_percent,
                   double price_incl_tax, double price_excl_tax,
                   String display_list_price, String display_standard_price, String display_margin, String display_margin_percent,
                   String display_price_incl_tax, String display_price_excl_tax, POS_Category category) {
        this.id = id;
        this.product_id = product_id;
        this.product_tmpl_id = product_tmpl_id;
        this.name = name;
        this.default_code = default_code;
        this.list_price = list_price;
        this.standard_price = standard_price;
        this.margin = margin;
        this.margin_percent = margin_percent;
        this.price_incl_tax = price_incl_tax;
        this.price_excl_tax = price_excl_tax;
        this.display_list_price = display_list_price;
        this.display_standard_price = display_standard_price;
        this.display_margin = display_margin;
        this.display_margin_percent = display_margin_percent;
        this.display_price_incl_tax = display_price_incl_tax;
        this.display_price_excl_tax = display_price_excl_tax;
        this.category = category;
    }
    public Product(){
        id = -1;
        product_id = -1;
        product_tmpl_id = -1;
        name = null;
        default_code = null;
        list_price = -1;
        standard_price = -1;
        margin = -1;
        margin_percent = -1;
        price_incl_tax = -1;
        price_excl_tax = -1;
        display_list_price = null;
        display_standard_price = null;
        display_margin = null;
        display_margin_percent = null;
        display_price_incl_tax = null;
        display_price_excl_tax = null;
        category = null;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id){ this.product_id = product_id; }

    public String getDisplay_list_price() {
        return display_list_price;
    }

    public void setDisplay_list_price(String display_list_price) {
        this.display_list_price = display_list_price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getList_price() {
        return list_price;
    }

    public void setList_price(double list_price) {
        this.list_price = list_price;
    }

    public String getDefault_code() {
        return default_code;
    }

    public void setDefault_code(String default_code) {
        this.default_code = default_code;
    }

    public double getStandard_price() {
        return standard_price;
    }

    public void setStandard_price(double standard_price) {
        this.standard_price = standard_price;
    }

    public double getPrice_incl_tax() {
        return price_incl_tax;
    }

    public void setPrice_incl_tax(double price_incl_tax) {
        this.price_incl_tax = price_incl_tax;
    }

    public double getPrice_excl_tax() {
        return price_excl_tax;
    }

    public void setPrice_excl_tax(double price_excl_tax) {
        this.price_excl_tax = price_excl_tax;
    }

    public String getDisplay_standard_price() {
        return display_standard_price;
    }

    public void setDisplay_standard_price(String display_standard_price) {
        this.display_standard_price = display_standard_price;
    }

    public String getDisplay_price_incl_tax() {
        return display_price_incl_tax;
    }

    public void setDisplay_price_incl_tax(String display_price_incl_tax) {
        this.display_price_incl_tax = display_price_incl_tax;
    }

    public String getDisplay_price_excl_tax() {
        return display_price_excl_tax;
    }

    public void setDisplay_price_excl_tax(String display_price_excl_tax) {
        this.display_price_excl_tax = display_price_excl_tax;
    }

    public double getMargin() {
        return margin;
    }

    public void setMargin(double margin) {
        this.margin = margin;
    }

    public double getMargin_percent() {
        return margin_percent;
    }

    public void setMargin_percent(double margin_percent) {
        this.margin_percent = margin_percent;
    }

    public String getDisplay_margin() {
        return display_margin;
    }

    public void setDisplay_margin(String display_margin) {
        this.display_margin = display_margin;
    }

    public String getDisplay_margin_percent() {
        return display_margin_percent;
    }

    public void setDisplay_margin_percent(String display_margin_percent) {
        this.display_margin_percent = display_margin_percent;
    }

    public POS_Category getCategory() {
        return category;
    }

    public void setCategory(POS_Category category) {
        this.category = category;
    }

    public int getProduct_tmpl_id() {
        return product_tmpl_id;
    }

    public void setProduct_tmpl_id(int product_tmpl_id) {
        this.product_tmpl_id = product_tmpl_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
