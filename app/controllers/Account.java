package controllers;

import controllers.Secure;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class Account extends Controller {
    public static void index() {
        render();
    }
}
