package controllers;

import models.User;
import play.cache.Cache;
import play.data.validation.Required;
import play.libs.Images;

public class Security extends Secure.Security {
    private static final String LAST_CAPTCHA_CODE_CACHE_KEY = "lastCaptchaCode";
    private static final String LAST_CAPTCHA_ID_CACHE_KEY = "lastCaptchaId";

    static boolean authenticate(String username, String password) {
        User user = User.find("byUsername", username).first();
        final boolean authenticated = user != null && user.passwordCorrect(password);
        return authenticated;
    }

    public static void captcha(@Required String id) {
        Images.Captcha captcha = createCaptcha(id);
        renderBinary(captcha);
    }

    /**
     * Creates a captcha and stores the solution into the Play cache with <code>id</code> as key.
     *
     * @param id the id to find the captcha solution in the Play cache
     * @return the created captcha
     */
    public static Images.Captcha createCaptcha(String id) {
        if (id == null) {
            throw new NullPointerException("captcha id is null");
        }
        Images.Captcha captcha = Images.captcha();
        String code = captcha.getText();
        Cache.set(id, code, "10mn");
        Cache.set(LAST_CAPTCHA_CODE_CACHE_KEY, code, "10mn");
        Cache.set(LAST_CAPTCHA_ID_CACHE_KEY, id, "10mn");
        return captcha;
    }

    public static String getLastCaptchaCode() {
        return Cache.get(LAST_CAPTCHA_CODE_CACHE_KEY).toString();
    }

    public static String getLastCaptchaId() {
        return Cache.get(LAST_CAPTCHA_ID_CACHE_KEY).toString();
    }

    public static User getCurrentUser() {
        /* not direct testable, because functional tests don't save cookies correct */
        final String username = Security.connected();
        return User.find("username", username).first();
    }
}