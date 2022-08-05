package com.findbulous.pos;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class POS_Session extends RealmObject {
    @PrimaryKey
    private int id;
    private String name, start_at, stop_at, state, opening_note;
    private int login_number, user_id;

    private POS_Config pos_config;

    public POS_Session(int id, String name, String start_at, String stop_at, String state, String opening_note,
                       int login_number, POS_Config pos_config, int user_id){
        this.id = id;
        this.name = name;
        this.start_at = start_at;
        this.stop_at = stop_at;
        this.state = state;
        this.opening_note = opening_note;
        this.login_number = login_number;
        this.pos_config = pos_config;
        this.user_id = user_id;
    }

    public POS_Session(){
        id = -1;
        name = null;
        start_at = null;
        stop_at = null;
        state = null;
        opening_note = null;
        login_number = -1;
        pos_config = null;
        user_id = -1;
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

    public String getStop_at() {
        return stop_at;
    }

    public void setStop_at(String stop_at) {
        this.stop_at = stop_at;
    }

    public String getOpening_note() {
        return opening_note;
    }

    public void setOpening_note(String opening_note) {
        this.opening_note = opening_note;
    }

    public int getLogin_number() {
        return login_number;
    }

    public void setLogin_number(int login_number) {
        this.login_number = login_number;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}
