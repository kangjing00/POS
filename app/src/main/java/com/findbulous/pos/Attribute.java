package com.findbulous.pos;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Attribute extends RealmObject {
    @PrimaryKey
    private int id;
    private String name, create_variant, display_type, visibility;
    private int sequence, product_tmpl_id, attribute_id, value_count, attribute_line_id;
    private boolean active;

    public Attribute(int id, String name, String create_variant, String display_type, String visibility,
                     int sequence, int product_tmpl_id, int attribute_id, int value_count, int attribute_line_id,
                     boolean active){
        this.id = id;
        this.name = name;
        this.create_variant = create_variant;
        this.display_type = display_type;
        this.visibility = visibility;
        this.sequence = sequence;
        this.product_tmpl_id = product_tmpl_id;
        this.attribute_id = attribute_id;
        this.value_count = value_count;
        this.attribute_line_id = attribute_line_id;
        this.active = active;
    }

    public Attribute(){
        id = -1;
        name = null;
        create_variant = null;
        display_type = null;
        visibility = null;
        sequence = -1;
        product_tmpl_id = -1;
        attribute_id = -1;
        value_count = -1;
        attribute_line_id = -1;
        active = false;
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

    public String getCreate_variant() {
        return create_variant;
    }

    public void setCreate_variant(String create_variant) {
        this.create_variant = create_variant;
    }

    public String getDisplay_type() {
        return display_type;
    }

    public void setDisplay_type(String display_type) {
        this.display_type = display_type;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public int getProduct_tmpl_id() {
        return product_tmpl_id;
    }

    public void setProduct_tmpl_id(int product_tmpl_id) {
        this.product_tmpl_id = product_tmpl_id;
    }

    public int getAttribute_id() {
        return attribute_id;
    }

    public void setAttribute_id(int attribute_id) {
        this.attribute_id = attribute_id;
    }

    public int getValue_count() {
        return value_count;
    }

    public void setValue_count(int value_count) {
        this.value_count = value_count;
    }

    public int getAttribute_line_id() {
        return attribute_line_id;
    }

    public void setAttribute_line_id(int attribute_line_id) {
        this.attribute_line_id = attribute_line_id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
