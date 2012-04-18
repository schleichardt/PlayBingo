package testutils;

import exceptions.BingoException;
import models.BingoGame;
import models.User;
import models.chat.ChatRoom;
import org.apache.commons.lang.time.DateUtils;

import java.util.Date;

public class BingoSeleniumTestUtil {

    public static final String GAME_TITLE = "A Bingo Game 1";
    public static final String GAME_TITLE_2 = "A Bingo Game 2";

    public static long prepareTest() throws BingoException {
        BingoGame game = initGame(GAME_TITLE);
        initGame(GAME_TITLE_2);
        User user = initUser(game);
        initChat(game, user);
        return game.getId();
    }

    private static void initChat(BingoGame game, User user) {
        final ChatRoom chatRoom = game.getChatRoom();
        chatRoom.appendMessageFrom("Bla 1", "One");
        chatRoom.appendMessageFrom("Bla 2", "Two");
        chatRoom.appendMessageFrom("Bla 3", "Three");
        chatRoom.save();
    }

    private static BingoGame initGame(String title) {
        BingoGame game = new BingoGame();
        game.setInitialWaitingTimeInSeconds(0);
        game.setDrawingIntervalInSeconds(1);
        game.setStarted(true);
        game.setStartingTime(DateUtils.addHours(new Date(), -1));
        game.setTitle(title);
        game.save();
        return game;
    }

    private static User initUser(BingoGame game) throws BingoException {
        User user = User.find("username", "Michael").first();
        user.buyCoupons(1);
        user.useUnusedCouponForGame(game);
        user.save();
        return user;
    }
}
