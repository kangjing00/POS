package com.findbulous.pos;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class POS_Category extends RealmObject {
    @PrimaryKey
    private int id;
    private String name;
    private int sequence, pos_categ_id;

    private RealmList<POS_Category> pos_categories;

    public POS_Category(int id, String name, int sequence, int pos_categ_id, RealmList<POS_Category> pos_categories){
        this.id = id;
        this.name = name;
        this.sequence = sequence;
        this.pos_categ_id = pos_categ_id;
        this.pos_categories = pos_categories;
    }

    public POS_Category(){
        id = -1;
        name = null;
        sequence = -1;
        pos_categ_id = -1;
        pos_categories = null;
    }

    public int getPos_categ_id() {
        return pos_categ_id;
    }

    public void setPos_categ_id(int pos_categ_id) {
        this.pos_categ_id = pos_categ_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RealmList<POS_Category> getPos_categories() {
        return pos_categories;
    }

    public void setPos_categories(RealmList<POS_Category> pos_categories) {
        this.pos_categories = pos_categories;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
}
