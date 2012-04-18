package controllers;

import models.BingoGame;
import models.Coupon;
import models.User;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import util.ListUtils;

import java.util.*;

public class BingoTest extends BingoAppFunctionalTest {

    @Before
    public void login() {
        super.login();
    }

    @Test
    public void testSuccessfulBuying() {
        User user = getUser();
        final int unusedCouponsAtStart = user.getUnusedCoupons();
        final Map<String, String> parameters = new HashMap<String, String>();
        final int numberOfCoupons = 6;
        parameters.put("count", Integer.toString(numberOfCoupons));
        final Http.Response response = POST("/bingo/buy/coupons", parameters);
        assertIsOk(response);
        user.refresh();
        final int actualNumberOfCoupons = user.getUnusedCoupons();
        assertEquals("coupons", unusedCouponsAtStart + numberOfCoupons, actualNumberOfCoupons);
    }

    @Test
    public void testInvalidBuying() {
        User user = getUser();
        final int unusedCouponsAtStart = user.getUnusedCoupons();
        final Map<String, String> parameters = new HashMap<String, String>();
        final int numberOfCoupons = -5;
        parameters.put("count", Integer.toString(numberOfCoupons));
        final Http.Response response = POST("/bingo/buy/coupons", parameters);
        assertIsOk(response);
        user.refresh();
        assertEquals("no additional coupons on invalid buying", unusedCouponsAtStart, user.getUnusedCoupons());
    }

    @Test
    public void testOpenBingoGame() {
        BingoGame game = createStartedGame();
        game.refresh();
        assertTrue("game has started", game.isStarted());
    }

    @Test
    public void testOpenBingoGameWithWrongUser() {
        BingoGame game = createPersistentGame();
        logout();
        login(USERNAME_TWO, PASSWORD);
        final String urlForGameStart = String.format("/bingo/start/game/%d", game.getId());
        final Http.Response responseForGameStart = GET(urlForGameStart);
        assertStatus(404, responseForGameStart);
        assertFalse("game cannot be started from another user", game.isStarted());
    }

    @Test
    public void testOpenBingoGameWithNonIntegerId() {
        final String urlForGameStart = "/bingo/start/game/nonsense";
        final Http.Response responseForGameStart = GET(urlForGameStart);
        assertStatus(404, responseForGameStart);
    }

    @Test
    public void testOpenBingoGameWithWrongId() {
        long id = 999999;
        final String urlForGameStart = String.format("/bingo/start/game/%d", id);
        final Http.Response responseForGameStart = GET(urlForGameStart);
        assertStatus(404, responseForGameStart);
    }

    @Test
    public void gameNotFound() {
        assertStatus(404, GET("/bingo/start/game/99999"));
        assertStatus(404, GET("/bingo/drawingsForGame/99999"));
    }

    @Test
    public void ajaxMarkCoupon() {
        final BingoGame bingoGame = startGameWithCoupons(3);
        final Coupon coupon = bingoGame.getCouponsForUserWithUsername(getUser().getUsername()).get(0);
        final int indexOfNumberOnCoupon = 3;
        final Integer numberOnCoupon = coupon.getNumbersAsList().get(indexOfNumberOnCoupon);
        final String urlMark = String.format("/coupon/true/%d/%d", coupon.getId(), numberOnCoupon);
        assertIsOk(GET(urlMark));
        coupon.refresh();
        assertTrue("number marked", coupon.getMarked(numberOnCoupon));

        assertIsOk(GET(urlMark));
        coupon.refresh();
        assertTrue("number marked (test idempotence)", coupon.getMarked(numberOnCoupon));

        final String urlUnmark = String.format("/coupon/false/%d/%d", coupon.getId(), numberOnCoupon);
        assertIsOk(GET(urlUnmark));
        coupon.refresh();
        assertFalse("number not marked anymore", coupon.getMarked(numberOnCoupon));
    }

    private BingoGame startGameWithCoupons(int numberOfCoupons) {
        BingoGame game = createStartedGame();
        buyCoupons(numberOfCoupons);
        addCouponsToGame(game.getId());
        game.refresh();
        return game;
    }

    private void addCouponsToGame(Long gameId) {
        final String urlForAjaxAddingCoupons = String.format("/bingo/add/coupon/to/game/%d", gameId);
        assertIsOk(GET(urlForAjaxAddingCoupons));
    }

    private void buyCoupons(int numberOfCoupons) {
        final User user = getUser();
        final int initialCoupons = user.getUnusedCoupons();
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("count", Integer.toString(numberOfCoupons));
        final Http.Response response = POST("/bingo/buy/coupons", parameters);
        assertIsOk(response);
        user.refresh();
        assertEquals(initialCoupons + numberOfCoupons, user.getUnusedCoupons());
    }

    @Test
    public void verifyWinner() {
        final BingoGame game = startGameWithCoupons(1);
        final String username = getUser().getUsername();
        final List<Integer> earlyWinningDrawing = generateNumbersWithEarliestPossibleWinningChance(game, username);
        game.setDrawings(earlyWinningDrawing);


        final int numberOfRequiredCorrectNumbers = game.getNumberOfRequiredCorrectNumbers();
        final Date dateBeforePossibleSuccess = game.getDateForDrawing(numberOfRequiredCorrectNumbers - 1);
        assertFalse("user can not have enough correct marked numbers", game.userCouldBeWinner(username, dateBeforePossibleSuccess));


        final Date dateAtPossibleSuccess = game.getDateForDrawing(numberOfRequiredCorrectNumbers);
        assertTrue("user with correct numbers should be winner", game.userCouldBeWinner(username, dateAtPossibleSuccess));
        final List<Integer> laterWinningDrawing = new ArrayList<Integer>(earlyWinningDrawing);
        Collections.reverse(laterWinningDrawing);

        game.setDrawings(laterWinningDrawing);
        assertFalse("user with incorrect numbers should not be winner", game.userCouldBeWinner(username, dateAtPossibleSuccess));
        final Date requiredDate = game.getDateForDrawing(game.getNumberOfRequiredCorrectNumbers());
        final long diff = requiredDate.getTime() - game.getFirstDrawingDate().getTime() - game.getInitialWaitingTimeInSeconds() * 1000;
        final Date newStartingDate = new Date(new Date().getTime() - diff);
        game.setStartingTime(newStartingDate);
        game.setDrawings(laterWinningDrawing);
        assertTrue(game.isStarted());
        assertFalse(game.isFinished());
        game.save();
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("gameId", Long.toString(game.getId()));
        assertIsOk(POST("/bingo/call/bingo", parameters));
        game.refresh();
        assertFalse("no valid numbers => game should continue", game.isFinished());
        game.setDrawings(earlyWinningDrawing);
        game.save();
        assertIsOk(POST("/bingo/call/bingo", parameters));
        game.refresh();
        assertTrue("valid numbers => game finished", game.isFinished());
    }

    private List<Integer> generateNumbersWithEarliestPossibleWinningChance(BingoGame game, String username) {
        final List<Coupon> couponList = game.getCouponsForUserWithUsername(username);
        final List<Integer> couponNumbers = couponList.get(0).getNumbersAsList();
        final List<Integer> earlyWinningDrawing = new ArrayList<Integer>(game.getNumberOfRequiredCorrectNumbers());
        for (int i = 0; i < couponNumbers.size(); i++) {
            if (BingoGame.winningPattern.get(i)) {
                earlyWinningDrawing.add(couponNumbers.get(i));
            }
        }
        Collections.shuffle(earlyWinningDrawing);
        earlyWinningDrawing.addAll(ListUtils.createListWithNumbersFromTo(1, BingoGame.NUMBERS_IN_GAME - earlyWinningDrawing.size()));
        return earlyWinningDrawing;
    }
}
