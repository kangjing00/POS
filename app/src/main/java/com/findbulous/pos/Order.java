package com.findbulous.pos;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;

public class Order  extends RealmObject {

    @PrimaryKey
    private int local_order_id;
    private int order_id;
    private String name, date_order, pos_reference, note, state, state_name; //state: "paid", "draft" ("OnGoing")
    private double amount_tax, amount_total, amount_paid, amount_return, amount_subtotal, tip_amount, discount;
    private boolean is_tipped;
    private int customer_count;
    private String discount_type;   //percentage /or/ fixed_amount
    private int session_id, user_id, company_id, partner_id;

    private String display_amount_tax, display_amount_total, display_amount_paid, display_amount_return,
                    display_amount_subtotal, display_tip_amount;

    private Table table;
    private Customer customer;

    @LinkingObjects("order")
    private final RealmResults<Order_Line> order_lines = null;

    //Constructors
    public Order(Order order){
        local_order_id = order.getLocal_order_id();
        order_id = order.getOrder_id();
        name = order.getName();
        date_order = order.getDate_order();
        pos_reference = order.getPos_reference();
        state = order.getState();
        state_name = order.getState_name();
        amount_tax = order.getAmount_tax();
        amount_total = order.getAmount_total();
        amount_paid = order.getAmount_paid();
        amount_return = order.getAmount_return();
        tip_amount = order.getTip_amount();
        display_amount_tax = order.getDisplay_amount_tax();
        display_amount_total = order.getDisplay_amount_total();
        display_amount_paid = order.getDisplay_amount_paid();
        display_amount_return = order.getDisplay_amount_return();
        display_amount_subtotal = order.getDisplay_amount_subtotal();
        display_tip_amount = order.getDisplay_tip_amount();
        is_tipped = order.isIs_tipped();
        table = order.getTable();
        customer = order.getCustomer();
        note = order.getNote();
        discount = order.getDiscount();
        discount_type = order.getDiscount_type();
        customer_count = order.getCustomer_count();
        session_id = order.getSession_id();
        user_id = order.getUser_id();
        company_id = order.getCompany_id();
        partner_id = order.getPartner_id();
    }
    public Order(int local_order_id, int order_id, String name, String date_order, String pos_reference, String state, String state_name,
                 double amount_tax, double amount_total, double amount_paid, double amount_return, double amount_subtotal,
                 double tip_amount, String display_amount_tax, String display_amount_total, String display_amount_paid,
                 String display_amount_return, String display_amount_subtotal, String display_tip_amount,
                 boolean is_tipped, Table table, Customer customer, String note, double discount,
                 String discount_type, int customer_count, int session_id, int user_id, int company_id, int partner_id){
        this.local_order_id = local_order_id;
        this.order_id = order_id;
        this.name = name;
        this.date_order = date_order;
        this.pos_reference = pos_reference;
        this.state = state;
        this.state_name = state_name;
        this.amount_tax = amount_tax;
        this.amount_total = amount_total;
        this.amount_paid = amount_paid;
        this.amount_return = amount_return;
        this.amount_subtotal = amount_subtotal;
        this.tip_amount = tip_amount;
        this.display_amount_tax = display_amount_tax;
        this.display_amount_total = display_amount_total;
        this.display_amount_paid = display_amount_paid;
        this.display_amount_return = display_amount_return;
        this.display_amount_subtotal = display_amount_subtotal;
        this.display_tip_amount = display_tip_amount;
        this.is_tipped = is_tipped;
        this.table = table;
        this.customer = customer;
        this.note = note;
        this.discount = discount;
        this.discount_type = discount_type;
        this.customer_count = customer_count;
        this.session_id = session_id;
        this.user_id = user_id;
        this.company_id = company_id;
        this.partner_id = partner_id;
    }

    public Order(){
        local_order_id = -1;
        order_id = -1;
        name = null;
        date_order = null;
        pos_reference = null;
        state = null;
        state_name = null;
        amount_tax = 0.0;
        amount_total = 0.0;
        amount_paid = 0.0;
        amount_return = 0.0;
        tip_amount = 0.0;
        display_amount_tax = null;
        display_amount_total = null;
        display_amount_paid = null;
        display_amount_return = null;
        display_amount_subtotal = null;
        display_tip_amount = null;
        is_tipped = false;
        table = null;
        customer = null;
        note = null;
        discount = 0.0;
        discount_type = null;
        customer_count = 0;
        session_id = -1;
        user_id = -1;
        company_id = -1;
        partner_id = -1;
    }

    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(int order_id){ this.order_id = order_id; }

    public String getDate_order() {
        return date_order;
    }

    public void setDate_order(String date_order) {
        this.date_order = date_order;
    }

    public RealmResults<Order_Line> getOrder_lines() {
        return order_lines;
    }

    public double getAmount_total() {
        return amount_total;
    }

    public void setAmount_total(double amount_total) {
        this.amount_total = amount_total;
    }

    public double getAmount_paid() {
        return amount_paid;
    }

    public void setAmount_paid(double amount_paid) {
        this.amount_paid = amount_paid;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public double getAmount_tax() {
        return amount_tax;
    }

    public void setAmount_tax(double amount_tax) {
        this.amount_tax = amount_tax;
    }

    public double getTip_amount() {
        return tip_amount;
    }

    public void setTip_amount(double tip_amount) {
        this.tip_amount = tip_amount;
    }

    public boolean isIs_tipped() {
        return is_tipped;
    }

    public void setIs_tipped(boolean is_tipped) {
        this.is_tipped = is_tipped;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getCustomer_count() {
        return customer_count;
    }

    public void setCustomer_count(int customer_count) {
        this.customer_count = customer_count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPos_reference() {
        return pos_reference;
    }

    public void setPos_reference(String pos_reference) {
        this.pos_reference = pos_reference;
    }

    public String getState_name() {
        return state_name;
    }

    public void setState_name(String state_name) {
        this.state_name = state_name;
    }

    public double getAmount_return() {
        return amount_return;
    }

    public void setAmount_return(double amount_return) {
        this.amount_return = amount_return;
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

    public int getSession_id() {
        return session_id;
    }

    public void setSession_id(int session_id) {
        this.session_id = session_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getCompany_id() {
        return company_id;
    }

    public void setCompany_id(int company_id) {
        this.company_id = company_id;
    }

    public int getPartner_id() {
        return partner_id;
    }

    public void setPartner_id(int partner_id) {
        this.partner_id = partner_id;
    }

    public int getLocal_order_id() {
        return local_order_id;
    }

    public void setLocal_order_id(int local_order_id) {
        this.local_order_id = local_order_id;
    }

    public double getAmount_subtotal() {
        return amount_subtotal;
    }

    public void setAmount_subtotal(double amount_subtotal) {
        this.amount_subtotal = amount_subtotal;
    }

    public String getDisplay_amount_tax() {
        return display_amount_tax;
    }

    public void setDisplay_amount_tax(String display_amount_tax) {
        this.display_amount_tax = display_amount_tax;
    }

    public String getDisplay_amount_total() {
        return display_amount_total;
    }

    public void setDisplay_amount_total(String display_amount_total) {
        this.display_amount_total = display_amount_total;
    }

    public String getDisplay_amount_paid() {
        return display_amount_paid;
    }

    public void setDisplay_amount_paid(String display_amount_paid) {
        this.display_amount_paid = display_amount_paid;
    }

    public String getDisplay_amount_return() {
        return display_amount_return;
    }

    public void setDisplay_amount_return(String display_amount_return) {
        this.display_amount_return = display_amount_return;
    }

    public String getDisplay_amount_subtotal() {
        return display_amount_subtotal;
    }

    public void setDisplay_amount_subtotal(String display_amount_subtotal) {
        this.display_amount_subtotal = display_amount_subtotal;
    }

    public String getDisplay_tip_amount() {
        return display_tip_amount;
    }

    public void setDisplay_tip_amount(String display_tip_amount) {
        this.display_tip_amount = display_tip_amount;
    }
}
