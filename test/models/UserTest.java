package models;

import org.junit.Test;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.test.UnitTest;
import validation.UniqueCheck;

public class UserTest extends UnitTest {

    @Test
    public void testValidation() {
        User user = new User();
        final Validation.ValidationResult validationResult = Validation.current().valid(user);
        if (validationResult.ok) {
            fail("user shouldn't be valid");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void sillyArgumentsOnBuyCoupons() {
        User user = new User();
        user.buyCoupons(-2);
    }

    @Test
    public void uniqueEmail() {
        User user = new User();
        final String email = "michael@schleichardt.net";
        user.setEmail(email);
        user.save();
        user.em().flush();
        User user2 = new User();
        user2.setEmail(email);
        Validation.current().valid("user2", user2);
        final String actualErrorMessage = Validation.current().errors("user2.email").get(0).message();
        final String expectedErrorMessage = Messages.get(UniqueCheck.message, "user2.email");
        assertEquals(actualErrorMessage, expectedErrorMessage);
    }
}