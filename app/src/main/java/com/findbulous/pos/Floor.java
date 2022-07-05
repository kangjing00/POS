package com.findbulous.pos;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;

public class Floor extends RealmObject {

    @PrimaryKey
    private int floor_id;
    private String name;
    private int sequence;
    private String active;
    @LinkingObjects("floor")
    private final RealmResults<Table> tables = null;

    public Floor(int floor_id, String name, int sequence, String active){
        this.floor_id = floor_id;
        this.name = name;
        this.sequence = sequence;
        this.active = active;
    }

    public Floor(){
        floor_id = -1;
        name = null;
        sequence = -1;
        active = null;
    }

    public int getFloor_id() {
        return floor_id;
    }

    public void setFloor_id(int floor_id) {
        this.floor_id = floor_id;
    }

    public RealmResults<Table> getTables() {
        return tables;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }
}
