package com.findbulous.pos;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class State extends RealmObject {
    @PrimaryKey
    private int state_id;
    private String code, name;

    public State(int state_id, String code, String name){
        this.state_id = state_id;
        this.code = code;
        this.name = name;
    }

    public State(){
        state_id = -1;
        code = null;
        name = null;
    }

    public int getState_id() {
        return state_id;
    }

    public void setState_id(int state_id) {
        this.state_id = state_id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
