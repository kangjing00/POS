package com.findbulous.pos;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class POS_Category extends RealmObject {
    @PrimaryKey
    private int pos_categ_id;
    private String name;

    private RealmList<POS_Category> pos_categories;

    public POS_Category(int pos_categ_id, String name, RealmList<POS_Category> pos_categories){
        this.pos_categ_id = pos_categ_id;
        this.name = name;
        this.pos_categories = pos_categories;
    }

    public POS_Category(){
        pos_categ_id = -1;
        name = null;
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
}
