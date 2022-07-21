package com.findbulous.pos;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;

public class Floor extends RealmObject {

    @PrimaryKey
    private int id;
    private String name;
    private int pos_config_id, sequence;
    private boolean active;
    private int floor_id;
    @LinkingObjects("floor")
    private final RealmResults<Table> tables = null;

    public Floor(int id, String name, int pos_config_id, int sequence, boolean active, int floor_id){
        this.id = id;
        this.name = name;
        this.pos_config_id = pos_config_id;
        this.sequence = sequence;
        this.active = active;
        this.floor_id = floor_id;
    }

    public Floor(){
        id = -1;
        name = null;
        pos_config_id = -1;
        sequence = -1;
        active = false;
        floor_id = -1;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPos_config_id() {
        return pos_config_id;
    }

    public void setPos_config_id(int pos_config_id) {
        this.pos_config_id = pos_config_id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
