package controllers;

import models.User;
import notifier.PasswordForgottenMailer;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.cache.Cache;
import play.data.validation.Required;
import play.libs.Mail;
import play.mvc.Controller;

public class PasswordForgotten extends Controller {

    public static final String TEMPLATE_PASSWORD_FORGOTTON_FORM = "PasswordForgotten/forgotPassword.html";

    public static void forgotPassword(@Required String email, @Required String captcha, @Required String captchaId) {
        if (!StringUtils.equals(captcha, Cache.get(captchaId, String.class))) {
            validation.addError("captcha", "validation.captchaDontMatches");
        }
        boolean success = false;
        if (validation.hasErrors()) {
            render(email, success);
        } else {
            User user = User.find("byEmail", email).first();
            String emailText = null;
            if (user != null) {
                final String secret = RandomStringUtils.random(60);
                final String key = getCacheKeyForSecret(secret);
                Cache.set(key, user.getId(), "30mn");
                Cache.set("lastChangePasswordSecretKey", key);
                PasswordForgottenMailer.sendEmail(user, secret);
                success = true;
                if (Play.mode.isDev()) {
                    emailText = Mail.Mock.getLastMessageReceivedBy(user.getEmail());//for presentation without email server
                }
            }
            render(success, emailText);
        }

    }

    public static void forgotPasswordForm() {
        render("PasswordForgotten/forgotPassword.html");
    }

    public static void changePasswordForm(String secret) {
        render(secret);
    }

    public static void changePassword(@Required String secret, @Required User user) {
        if (validation.hasErrors()) {
            render(TEMPLATE_PASSWORD_FORGOTTON_FORM);
        }

        final String newPassword = user.getPassword();
        final String key = getCacheKeyForSecret(secret);
        final Long id = Cache.get(key, Long.class);
        if (id != null) {
            user = User.findById(id);
            if (user != null) {
                user.setPassword(newPassword);
                if (user.validateAndSave()) {
                    Cache.delete(key);
                    render();
                } else {
                    render("PasswordForgotten/changePasswordForm.html", secret, user);
                }

            } else {
                render(TEMPLATE_PASSWORD_FORGOTTON_FORM);
            }
        } else {
            render("PasswordForgotten/forgotPassword.html");
        }
    }

    public static String getCacheKeyForSecret(String secret) {
        return PasswordForgotten.class.getName() + "_secret_" + secret;
    }

    public static String getLastSecretEntryKey() {
        return (String) Cache.get("lastChangePasswordSecretKey");
    }
}
