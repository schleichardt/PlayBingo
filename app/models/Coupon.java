package models;

import play.db.jpa.Model;
import util.ListUtils;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
public class Coupon extends Model {
    private static final int NUMBER_OF_VALUES = 24;

    @ManyToOne(optional = true)
    private BingoGame game;

    @ManyToOne
    private User owner;

    @OneToMany(cascade = CascadeType.ALL)
    private List<BingoCouponNumber> numbers = new ArrayList<BingoCouponNumber>(NUMBER_OF_VALUES);

    public Coupon() {
        List<Integer> drawings = ListUtils.createListWithNumbersFromTo(1, BingoGame.NUMBERS_IN_GAME);
        Collections.shuffle(drawings);
        for (int i = 0; i < NUMBER_OF_VALUES; i++) {
            final Integer value = drawings.get(i);
            BingoCouponNumber number = new BingoCouponNumber(value);
            numbers.add(number);
        }
    }

    public Coupon(BingoGame game, User owner) {
        this();
        setGame(game);
        setOwner(owner);
    }

    public BingoGame getGame() {
        return game;
    }

    public void setGame(BingoGame game) {
        this.game = game;
    }

    public List<Integer> getNumbersAsList() {
        List<Integer> list = new ArrayList<Integer>(numbers.size());
        for (BingoCouponNumber number : numbers) {
            list.add(number.getValue());
        }
        return list;
    }

    public void setMarked(int number, boolean marked) {
        final BingoCouponNumber numberObject = getBingoCouponNumber(number);
        if (numberObject != null) {
            numberObject.setMarked(marked);
        }
    }

    public boolean getMarked(int number) {
        boolean marked = false;
        final BingoCouponNumber numberObject = getBingoCouponNumber(number);
        if (numberObject != null) {
            marked = numberObject.isMarked();
        }
        return marked;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    private BingoCouponNumber getBingoCouponNumber(int number) {
        BingoCouponNumber result = null;
        for (int i = 0; result == null && i < numbers.size(); i++) {
            if (numbers.get(i).getValue() == number) {
                result = numbers.get(i);
            }
        }
        return result;
    }
}