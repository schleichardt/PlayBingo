package controllers;

import models.BingoGame;
import models.User;
import org.junit.Before;
import play.libs.Codec;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;

public abstract class BingoAppFunctionalTest extends FunctionalTest {

    protected static final String USERNAME = "Michael";
    protected static final String PASSWORD = "Play1Rules.";
    protected static final String USERNAME_TWO = "usertwo";

    protected BingoGame createPersistentGame() {
        final long numberOfInitialBingoGames = BingoGame.count();
        final int initialWaitingTimeInSeconds = 0;
        final int drawingIntervalInSeconds = 1;

        assertIsOk(GET("/bingo/open/game"));

        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("game.initialWaitingTimeInSeconds", Integer.toString(initialWaitingTimeInSeconds));
        parameters.put("game.drawingIntervalInSeconds", Integer.toString(drawingIntervalInSeconds));
        final String gameTitle = "My Game " + Codec.UUID();
        parameters.put("game.title", gameTitle);
        final Response response = POST("/bingo/open/game", parameters);
        assertIsOk(response);

        assertEquals("a new Bingo Game was created, so there should exist one more game", numberOfInitialBingoGames + 1, BingoGame.count());
        BingoGame game = BingoGame.find("title", gameTitle).first();
        assertEquals("initialWaitingTimeInSeconds matches", initialWaitingTimeInSeconds, game.getInitialWaitingTimeInSeconds());
        assertEquals("drawingIntervalInSeconds matches", drawingIntervalInSeconds, game.getDrawingIntervalInSeconds());
        assertEquals("title matches", gameTitle, game.getTitle());
        return game;
    }

    protected BingoGame createStartedGame() {
        BingoGame game = createPersistentGame();
        final String urlForGameStart = String.format("/bingo/start/game/%d", game.getId());
        assertIsOk(GET(urlForGameStart));
        return game;
    }

    @Before
    public void init() {
        Fixtures.deleteAllModels();
        Fixtures.loadModels("initial-data.yml");
    }

    protected void login() {
        login(USERNAME, PASSWORD);
    }

    protected Response login(String username, String password) {
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("username", username);
        parameters.put("password", password);
        return POST("/login", parameters);
    }

    protected void logout() {
        GET("/logout");
    }


    protected User getUser() {
        return User.find("byUsername", USERNAME).first();
    }

    public static void assertIsOk(Response response) {
        if (!(response.status == 200 || response.status == 302)) {
            fail("response not okay");
        }
    }
}
