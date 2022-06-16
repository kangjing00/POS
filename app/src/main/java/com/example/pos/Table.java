package com.example.pos;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

public class Table extends RealmObject {
    @PrimaryKey
    private int table_id;
    private String table_name;
    private boolean vacant, onHold, occupied, active;

    //Constructor
    public Table(int table_id, String table_name, boolean vacant, boolean onHold, boolean occupied, boolean active){
        this.table_id = table_id;
        this.table_name = table_name;
        this.vacant = vacant;
        this.onHold = onHold;
        this.occupied = occupied;
        this.active = active;
    }
    public Table(){
        table_id = -1;
        table_name = null;
        vacant = true;
        onHold = false;
        occupied = false;
        active = true;
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
}
