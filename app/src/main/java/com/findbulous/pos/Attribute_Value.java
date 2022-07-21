package com.findbulous.pos;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Attribute_Value extends RealmObject {
    @PrimaryKey
    private int id;
    private String name, html_color;
    private int sequence, attribute_id, color, product_attribute_value_id, attribute_line_id,
            product_tmpl_id, product_template_attribute_value_id;
    private boolean ptav_active;
    private double price_extra;
    private String display_price_extra;

    public Attribute_Value(int id, String name, String html_color, int sequence, int attribute_id,
                           int color, int product_attribute_value_id, int attribute_line_id,
                           int product_tmpl_id, int product_template_attribute_value_id,
                           boolean ptav_active, double price_extra, String display_price_extra){
        this.id = id;
        this.name = name;
        this.html_color = html_color;
        this.sequence = sequence;
        this.attribute_id = attribute_id;
        this.color = color;
        this.product_attribute_value_id = product_attribute_value_id;
        this.attribute_line_id = attribute_line_id;
        this.product_tmpl_id = product_tmpl_id;
        this.product_template_attribute_value_id = product_template_attribute_value_id;
        this.ptav_active = ptav_active;
        this.price_extra = price_extra;
        this.display_price_extra = display_price_extra;
    }

    public Attribute_Value(){
        id = -1;
        name = null;
        html_color = null;
        sequence = -1;
        attribute_id = -1;
        color = -1;
        product_attribute_value_id = -1;
        attribute_line_id = -1;
        product_tmpl_id = -1;
        product_template_attribute_value_id = -1;
        ptav_active = false;
        price_extra = -1;
        display_price_extra = null;
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

    public String getHtml_color() {
        return html_color;
    }

    public void setHtml_color(String html_color) {
        this.html_color = html_color;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public int getAttribute_id() {
        return attribute_id;
    }

    public void setAttribute_id(int attribute_id) {
        this.attribute_id = attribute_id;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getProduct_attribute_value_id() {
        return product_attribute_value_id;
    }

    public void setProduct_attribute_value_id(int product_attribute_value_id) {
        this.product_attribute_value_id = product_attribute_value_id;
    }

    public int getAttribute_line_id() {
        return attribute_line_id;
    }

    public void setAttribute_line_id(int attribute_line_id) {
        this.attribute_line_id = attribute_line_id;
    }

    public int getProduct_tmpl_id() {
        return product_tmpl_id;
    }

    public void setProduct_tmpl_id(int product_tmpl_id) {
        this.product_tmpl_id = product_tmpl_id;
    }

    public int getProduct_template_attribute_value_id() {
        return product_template_attribute_value_id;
    }

    public void setProduct_template_attribute_value_id(int product_template_attribute_value_id) {
        this.product_template_attribute_value_id = product_template_attribute_value_id;
    }

    public boolean isPtav_active() {
        return ptav_active;
    }

    public void setPtav_active(boolean ptav_active) {
        this.ptav_active = ptav_active;
    }

    public double getPrice_extra() {
        return price_extra;
    }

    public void setPrice_extra(double price_extra) {
        this.price_extra = price_extra;
    }

    public String getDisplay_price_extra() {
        return display_price_extra;
    }

    public void setDisplay_price_extra(String display_price_extra) {
        this.display_price_extra = display_price_extra;
    }
}
