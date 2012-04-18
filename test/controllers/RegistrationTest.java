package controllers;

import models.User;
import org.junit.Test;
import play.mvc.Http;
import testutils.EmailExtractionUtil;

import java.util.HashMap;
import java.util.Map;

public class RegistrationTest extends BingoAppFunctionalTest {

    @Test
    public void testPersistence() {
        final long initialNumberOfUsers = User.count();
        Map<String, String> userParams = new HashMap<String, String>();
        final String username = "Testuser";
        userParams.put("user.username", username);
        final String password = "Ab12,-abcd";
        userParams.put("user.password", password);
        final String email = "test@ba.schleichardt.info";
        userParams.put("user.email", email);
        Http.Response createResponse = POST("/register", userParams);
        assertEquals("one user created, one more should be in the database", initialNumberOfUsers + 1, User.count());
        User persistedUser = User.find("byUsername", username).first();
        assertEquals("username correct", username, persistedUser.getUsername());
        assertEquals("email correct", email, persistedUser.getEmail());
        assertTrue("password correct", persistedUser.passwordCorrect(password));
        assertFalse("email is not confirmed before clicking the link from email", persistedUser.isEmailConfirmed());

        String emailConfirmationLink = EmailExtractionUtil.extractLink(email);

        //wrong key
        GET(emailConfirmationLink + "x");
        persistedUser.refresh();
        assertFalse("email is not confirmed after clicking a wrong link", persistedUser.isEmailConfirmed());

        GET(emailConfirmationLink);
        persistedUser.refresh();
        assertTrue("email is confirmed after clicking the link from email", persistedUser.isEmailConfirmed());
        persistedUser.delete();
    }
}