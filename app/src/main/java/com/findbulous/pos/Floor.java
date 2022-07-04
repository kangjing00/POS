package com.findbulous.pos;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;

public class Floor extends RealmObject {

    @PrimaryKey
    private int floor_id;
    private String floor_name;
    private boolean active;
    private int noRow, noColumn;
    @LinkingObjects("floor")
    private final RealmResults<Table> tables = null;

    public Floor(int floor_id, String floor_name, boolean active, int noRow, int noColumn){
        this.floor_id = floor_id;
        this.floor_name = floor_name;
        this.active = active;
        this.noRow = noRow;
        this.noColumn = noColumn;
    }

    public Floor(){
        floor_id = -1;
        floor_name = null;
        active = false;
        noRow = 0;
        noColumn = 0;
    }

    public int getFloor_id() {
        return floor_id;
    }

    public void setFloor_id(int floor_id) {
        this.floor_id = floor_id;
    }

    public String getFloor_name() {
        return floor_name;
    }

    public void setFloor_name(String floor_name) {
        this.floor_name = floor_name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public RealmResults<Table> getTables() {
        return tables;
    }

    public int getNoRow() {
        return noRow;
    }

    public void setNoRow(int noRow) {
        this.noRow = noRow;
    }

    public int getNoColumn() {
        return noColumn;
    }

    public void setNoColumn(int noColumn) {
        this.noColumn = noColumn;
    }
}
