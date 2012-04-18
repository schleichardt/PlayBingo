package models;

import play.db.jpa.Model;

import javax.persistence.Entity;

@Entity
public class BingoCouponNumber extends Model {
    private final int value;
    private boolean marked = false;

    public BingoCouponNumber(int value) {
        this.value = value;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public int getValue() {
        return value;
    }
}
