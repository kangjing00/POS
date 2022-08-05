package com.findbulous.pos;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class POS_Config extends RealmObject {
    @PrimaryKey
    private int id;
    private String name;
    private boolean is_table_management, iface_tipproduct, iface_orderline_customer_notes,
            iface_orderline_notes, manual_discount, product_configurator;
    private int iface_start_categ_id, company_id;

    public POS_Config(int id, String name, boolean is_table_management, boolean iface_tipproduct, boolean iface_orderline_customer_notes,
                      int iface_start_categ_id, boolean iface_orderline_notes, boolean manual_discount, boolean product_configurator,
                      int company_id){
        this.id = id;
        this.name = name;
        this.is_table_management = is_table_management;
        this.iface_tipproduct = iface_tipproduct;
        this.iface_orderline_customer_notes = iface_orderline_customer_notes;
        this.iface_start_categ_id = iface_start_categ_id;
        this.iface_orderline_notes = iface_orderline_notes;
        this.manual_discount = manual_discount;
        this.product_configurator = product_configurator;
        this.company_id = company_id;
    }

    public POS_Config(){
        id = -1;
        name = null;
        is_table_management = false;
        iface_tipproduct = false;
        iface_orderline_customer_notes = false;
        iface_start_categ_id = -1;
        iface_orderline_notes = false;
        manual_discount = false;
        product_configurator = false;
        company_id = -1;
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

    public boolean isIs_table_management() {
        return is_table_management;
    }

    public void setIs_table_management(boolean is_table_management) {
        this.is_table_management = is_table_management;
    }

    public boolean isIface_tipproduct() {
        return iface_tipproduct;
    }

    public void setIface_tipproduct(boolean iface_tipproduct) {
        this.iface_tipproduct = iface_tipproduct;
    }

    public boolean isIface_orderline_customer_notes() {
        return iface_orderline_customer_notes;
    }

    public void setIface_orderline_customer_notes(boolean iface_orderline_customer_notes) {
        this.iface_orderline_customer_notes = iface_orderline_customer_notes;
    }

    public boolean isManual_discount() {
        return manual_discount;
    }

    public void setManual_discount(boolean manual_discount) {
        this.manual_discount = manual_discount;
    }

    public boolean isIface_orderline_notes() {
        return iface_orderline_notes;
    }

    public void setIface_orderline_notes(boolean iface_orderline_notes) {
        this.iface_orderline_notes = iface_orderline_notes;
    }

    public int getIface_start_categ_id() {
        return iface_start_categ_id;
    }

    public void setIface_start_categ_id(int iface_start_categ_id) {
        this.iface_start_categ_id = iface_start_categ_id;
    }

    public boolean isProduct_configurator() {
        return product_configurator;
    }

    public void setProduct_configurator(boolean product_configurator) {
        this.product_configurator = product_configurator;
    }

    public int getCompany_id() {
        return company_id;
    }

    public void setCompany_id(int company_id) {
        this.company_id = company_id;
    }
}
