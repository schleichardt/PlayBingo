package controllers;

import models.User;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import play.cache.Cache;
import play.libs.Codec;
import play.libs.Mail;
import testutils.EmailExtractionUtil;

import java.util.HashMap;
import java.util.Map;

public class PasswordForgottenTest extends BingoAppFunctionalTest {

    private static final String EMAIL = "michael@schleichardt.net";
    private static final String NEW_PASSWORD = "123Abc.-456";
    private static final String OLD_PASSWORD = "Play1Rules.";
    private static final String PASSWORD_FORGOTTEN_URL = "/forgotten/password";

    @Test
    public void testWithCorrectValues() {
        final User user = getUser();
        assertOnlyOldPasswordWorks(user);
        requestEmailWithHyperlink();
        assertTrue(emailWasSent());
        tryToChangePassword();
        assertOnlyNewPasswordWorks(user);
    }

    @Test
    public void testTimeout() {
        final User user = getUser();
        assertOnlyOldPasswordWorks(user);
        requestEmailWithHyperlink();
        assertTrue(emailWasSent());
        final String lastSecretEntryKey = PasswordForgotten.getLastSecretEntryKey();
        Cache.safeDelete(lastSecretEntryKey);
        tryToChangePassword();
        assertOnlyOldPasswordWorks(user);
    }

    @Test
    public void testUserDeletedAfterRequest() {
        final User user = getUser();
        assertOnlyOldPasswordWorks(user);
        requestEmailWithHyperlink();
        assertTrue(emailWasSent());
        user.delete();
        tryToChangePassword();
        assertOnlyOldPasswordWorks(user);
    }

    @Test
    public void testWithWrongEmail() {
        final User user = getUser();
        assertOnlyOldPasswordWorks(user);
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("email", "not" + EMAIL);
        parameters.put("captcha", Security.getLastCaptchaCode());
        parameters.put("captchaId", Security.getLastCaptchaId());
        POST(PASSWORD_FORGOTTEN_URL, parameters);
        assertFalse(emailWasSent(parameters.get("email")));
        assertOnlyOldPasswordWorks(user);
    }

    @Test
    public void testWithWrongCaptcha() {
        Mail.Mock.reset();
        assertFalse("email queue empty", emailWasSent());
        final User user = getUser();
        assertOnlyOldPasswordWorks(user);
        GET(PASSWORD_FORGOTTEN_URL);
        Security.createCaptcha(Codec.UUID());//for this test we don't render html so captcha image won't appear
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("email", EMAIL);
        parameters.put("captcha", "not" + Security.getLastCaptchaCode());
        parameters.put("captchaId", Security.getLastCaptchaId());
        POST(PASSWORD_FORGOTTEN_URL, parameters);
        assertFalse("captcha wrong so no email should has been sent", emailWasSent());
        assertOnlyOldPasswordWorks(user);
    }

    private Map<String, String> getCorrectParametersForPasswordChange() {
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("user.password", NEW_PASSWORD);
        return parameters;
    }


    private void assertOnlyNewPasswordWorks(User user) {
        user.refresh();
        assertTrue("new password works", user.passwordCorrect(NEW_PASSWORD));
        assertFalse("old password don't works", user.passwordCorrect(OLD_PASSWORD));
    }

    private void tryToChangePassword() {
        final String passwordChangeLink = EmailExtractionUtil.extractLink(EMAIL);
        GET(passwordChangeLink);
        final Map<String, String> parameters = getCorrectParametersForPasswordChange();
        POST(passwordChangeLink, parameters);
    }

    private void assertOnlyOldPasswordWorks(User user) {
        assertTrue("old password works", user.passwordCorrect(OLD_PASSWORD));
        assertFalse("new password works not", user.passwordCorrect(NEW_PASSWORD));
    }

    private void requestEmailWithHyperlink() {
        requestEmailWithHyperlink(EMAIL);
    }

    private void requestEmailWithHyperlink(String email) {
        GET(PASSWORD_FORGOTTEN_URL);
        Security.createCaptcha(Codec.UUID());//for this test we don't render html so catcha image won't appear
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("email", email);
        parameters.put("captcha", Security.getLastCaptchaCode());
        parameters.put("captchaId", Security.getLastCaptchaId());
        POST(PASSWORD_FORGOTTEN_URL, parameters);
    }

    private boolean emailWasSent(String email) {
        return StringUtils.isNotEmpty(Mail.Mock.getLastMessageReceivedBy(email));
    }

    private boolean emailWasSent() {
        return emailWasSent(EMAIL);
    }
}
