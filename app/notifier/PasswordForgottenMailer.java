package notifier;

import models.User;
import play.data.validation.Required;
import play.i18n.Messages;
import play.mvc.Mailer;

public class PasswordForgottenMailer extends Mailer {

    public static boolean sendEmail(@Required User user, @Required String secret) {
        addRecipient(user.getEmail());
        setSubject(Messages.get("PasswordForgottenMailer.email.subject"));
        return sendAndWait(user, secret);
    }
}
