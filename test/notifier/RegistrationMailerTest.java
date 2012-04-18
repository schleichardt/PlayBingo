package notifier;

import models.User;
import org.apache.commons.lang.StringUtils;
import play.libs.Mail;
import play.test.*;
import org.junit.*;
import testutils.UserUtil;

public class RegistrationMailerTest extends UnitTest {

    @Test
    public void test() {
        User user = UserUtil.generateTestUser();
        RegistrationMailer.sendEmailConfirmationRequest(user);
        final String message = Mail.Mock.getLastMessageReceivedBy(user.getEmail());
        final String expectedRecipient = String.format("To: \"%s\"", user.getEmail());
        assertTrue("correct recipient", StringUtils.contains(message, expectedRecipient));
        assertTrue("correct key in mail", StringUtils.contains(message, user.getSecret()));
    }
}