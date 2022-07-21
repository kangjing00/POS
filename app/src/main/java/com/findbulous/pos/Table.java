package com.findbulous.pos;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Table extends RealmObject {
    @PrimaryKey
    private int table_id;
    private String name;
    private double position_h, position_v, width, height;
    private int seats;
    private boolean active;
    private String state; // V - vacant, H - on hold, O - occupied

    private Floor floor;

    //Constructor
    public Table(int table_id, String name, double position_h, double position_v, double width, double height, int seats, boolean active, String state, Floor floor){
        this.table_id = table_id;
        this.name = name;
        this.position_h = position_h;
        this.position_v = position_v;
        this.width = width;
        this.height = height;
        this.seats = seats;
        this.active = active;
        this.state = state;
        this.floor = floor;
    }
    public Table(){
        table_id = -1;
        name = null;
        position_h = -1;
        position_v = -1;
        width = -1;
        height = -1;
        seats = -1;
        active = false;
        state = null;
        floor = null;
    }

    public int getTable_id() {
        return table_id;
    }

    public void setTable_id(int table_id) {
        this.table_id = table_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPosition_h() {
        return position_h;
    }

    public void setPosition_h(double position_h) {
        this.position_h = position_h;
    }

    public double getPosition_v() {
        return position_v;
    }

    public void setPosition_v(double position_v) {
        this.position_v = position_v;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Floor getFloor() {
        return floor;
    }

    public void setFloor(Floor floor) {
        this.floor = floor;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
