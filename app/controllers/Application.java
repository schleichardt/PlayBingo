package controllers;

import play.data.validation.Required;
import play.mvc.Before;
import play.mvc.Controller;

public class Application extends Controller {

    @Before
    public static void setUpDefaultArg() {
        final boolean userIsLoggedIn = true;
        renderArgs.put("userIsLoggedIn", userIsLoggedIn);
    }

    public static void index() {
        render();
    }

    public static void rulesOfGame() {
        render();
    }

    public static void login(@Required String username, @Required String password) {
        renderTemplate("Application/index.html");
    }
}