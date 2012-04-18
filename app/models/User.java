package models;

import exceptions.BingoException;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import play.data.validation.*;
import play.db.jpa.Model;
import play.libs.Crypto;
import validation.ComplexPasswordCheck;
import validation.Unique;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
public class User extends Model {

    @Required
    @MinSize(2)
    @Column(unique = true)
    @Unique(message = "validation.user.uniqueUsername")
    @Match("[a-zA-Z]+")
    private String username;


    @MinSize(9)
    @MaxSize(20)
    @Transient
    @Password
    @CheckWith(ComplexPasswordCheck.class)
    private String password;

    @Required
    private String hashedPassword;

    @Required
    @Email
    @Unique
    private String email;

    /**
     * a secret for email confirmation
     */
    private String secret;
    private boolean emailConfirmed = false;

    private int unusedCoupons = 0;

    public User() {
        final int lengthOfSecretKey = 100;
        secret = RandomStringUtils.randomAlphanumeric(lengthOfSecretKey);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
        this.hashedPassword = Crypto.passwordHash(password);
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSecret() {
        return secret;
    }

    public boolean isEmailConfirmed() {
        return emailConfirmed;
    }

    public void setEmailConfirmed(boolean emailConfirmed) {
        this.emailConfirmed = emailConfirmed;
    }

    public boolean passwordCorrect(String password) {
        final boolean passwordCorrect = StringUtils.equals(hashedPassword, Crypto.passwordHash(password));
        return passwordCorrect;
    }

    public boolean confirmEmailWithSecret(String secret) {
        setEmailConfirmed(this.secret.equals(secret));
        return emailConfirmed;
    }

    /**
     * Buys a specified number of Bingo coupons. Does not save automatically.
     *
     * @param count number of Coupons to buy
     */
    public synchronized void buyCoupons(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("can't buy a negative number of coupons");
        }
        setUnusedCoupons(count + getUnusedCoupons());
    }

    public int getUnusedCoupons() {
        return unusedCoupons;
    }

    public void setUnusedCoupons(int unusedCoupons) {
        this.unusedCoupons = unusedCoupons;
    }

    public Coupon useUnusedCouponForGame(BingoGame game) throws BingoException {
        if (getUnusedCoupons() < 1) {
            throw new BingoException("bingo.game.noMoreCouponsLeft");
        }
        unusedCoupons--;
        save();
        Coupon coupon = new Coupon(game, this);
        coupon.save();
        return coupon;
    }
}