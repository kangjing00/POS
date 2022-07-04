package com.findbulous.pos;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Table extends RealmObject {
    @PrimaryKey
    private int table_id;
    private String table_name;
    private int seats;
    private boolean vacant, onHold, occupied, active;

    private Floor floor;

    //Constructor
    public Table(int table_id, String table_name, int seats, boolean vacant, boolean onHold, boolean occupied, boolean active, Floor floor){
        this.table_id = table_id;
        this.table_name = table_name;
        this.seats = seats;
        this.vacant = vacant;
        this.onHold = onHold;
        this.occupied = occupied;
        this.active = active;
        this.floor = floor;
    }
    public Table(){
        table_id = -1;
        table_name = null;
        seats = -1;
        vacant = true;
        onHold = false;
        occupied = false;
        active = true;
        floor = null;
    }

    public int getTable_id() {
        return table_id;
    }

    public void setTable_id(int table_id) {
        this.table_id = table_id;
    }

    public String getTable_name() {
        return table_name;
    }

    public void setTable_name(String table_name) {
        this.table_name = table_name;
    }

    public boolean isVacant() {
        return vacant;
    }

    public void setVacant(boolean vacant) {
        this.vacant = vacant;
    }

    public boolean isOnHold() {
        return onHold;
    }

    public void setOnHold(boolean onHold) {
        this.onHold = onHold;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public Floor getFloor() {
        return floor;
    }

    public void setFloor(Floor floor) {
        this.floor = floor;
    }
}
