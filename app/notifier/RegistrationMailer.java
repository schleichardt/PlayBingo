package notifier;

import models.User;
import play.i18n.Messages;
import play.mvc.Mailer;

public class RegistrationMailer extends Mailer {

    public static boolean sendEmailConfirmationRequest(User user) {
        addRecipient(user.getEmail());
        setSubject(Messages.get("RegistrationMailer.sendEmailConfirmationRequest.subject"));
        return sendAndWait(user);
    }
}