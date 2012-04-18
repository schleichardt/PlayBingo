package controllers;

import models.User;
import notifier.RegistrationMailer;
import play.Play;
import play.data.validation.Required;
import play.libs.Mail;
import play.mvc.Controller;

public class Registration extends Controller {

    public static void register(@Required User user) {
        validation.required("user.password", user.getPassword());
        validation.valid(user);
        if ("GET".equals(request.method)) {
            validation.clear();
            render();
        } else if (validation.hasErrors()) {
            render(user);
        } else {
            user.save();
            final boolean emailSuccessfullySend = RegistrationMailer.sendEmailConfirmationRequest(user);
            String emailText = null;
            if (Play.mode.isDev()) {
                emailText = Mail.Mock.getLastMessageReceivedBy(user.getEmail());//for presentation without email server
            }
            render("Registration/confirmRegistration.html", user, emailSuccessfullySend, emailText);
        }
    }

    public static void confirmEmail(@Required String key, @Required long userId) {
        final User user = User.findById(userId);
        boolean success = false;
        if (user != null && user.confirmEmailWithSecret(key)) {
            success = true;
            user.save();
        }
        render(success);
    }

}
