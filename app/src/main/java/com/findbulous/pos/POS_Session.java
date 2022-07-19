package com.findbulous.pos;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class POS_Session extends RealmObject {
    @PrimaryKey
    private int id;
    private String name, start_at, state;

    private POS_Config pos_config;

    public POS_Session(int id, String name, String start_at, String state, POS_Config pos_config){
        this.id = id;
        this.name = name;
        this.start_at = start_at;
        this.state = state;
        this.pos_config = pos_config;
    }

    public POS_Session(){
        id = -1;
        name = null;
        start_at = null;
        state = null;
        pos_config = null;
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

    public String getStart_at() {
        return start_at;
    }

    public void setStart_at(String start_at) {
        this.start_at = start_at;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public POS_Config getPos_config() {
        return pos_config;
    }

    public void setPos_config(POS_Config pos_config) {
        this.pos_config = pos_config;
    }
}
