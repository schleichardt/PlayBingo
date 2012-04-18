package models;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import play.libs.F;
import play.test.UnitTest;
import util.ListUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BingoGameTest extends UnitTest {
    BingoGame game;

    @Before
    public void initGame() {
        game = new BingoGame();
        game.setDrawingIntervalInSeconds(2);
        game.setInitialWaitingTimeInSeconds(3);
        game.setStarted(true);
    }

    @Test
    public void firstDrawingDateConstant() {
        final Date firstDrawingDate = game.getFirstDrawingDate();
        game.setStarted(true);
        assertEquals("startingDate should not change after started", firstDrawingDate, game.getFirstDrawingDate());
    }

    @Test
    public void drawingsGeneration() {
        final List<Integer> drawings = game.getDrawings();
        final List<Integer> reference = ListUtils.createListWithNumbersFromTo(1, BingoGame.NUMBERS_IN_GAME);
        assertEquals("75 values expected", 75, drawings.size());
        assertFalse("Bingo list should not be ordered", reference.equals(drawings));
        Collections.sort(drawings);
        assertTrue("Bingo list should be from 1 to 75", reference.equals(drawings));
    }

    @Test
    public void dateIsAfterFirstDrawing() {
        Date dateBeforeFirstDrawing = DateUtils.addSeconds(game.getFirstDrawingDate(), -1);
        assertFalse("date before first drawing", game.dateIsAfterFirstDrawing(dateBeforeFirstDrawing));
        Date dateAfterFirstDrawing = DateUtils.addSeconds(game.getFirstDrawingDate(), +1);
        assertTrue("date after first drawing", game.dateIsAfterFirstDrawing(dateAfterFirstDrawing));
    }

    @Test
    public void numberOfDrawnNumbersUntil() {
        final Date dateBeforeFirstDrawing = DateUtils.addSeconds(game.getFirstDrawingDate(), -1);
        assertEquals("no numbers drawn", 0, game.numberOfDrawnNumbersUntil(dateBeforeFirstDrawing));
        assertEquals("1 number drawn", 1, game.numberOfDrawnNumbersUntil(game.getFirstDrawingDate()));
        final Date dateAfterSecondNumber = DateUtils.addSeconds(game.getFirstDrawingDate(), 1 * game.getDrawingIntervalInSeconds() + 1);
        assertEquals("2 numbers drawn", 2, game.numberOfDrawnNumbersUntil(dateAfterSecondNumber));
        assertEquals("2 numbers drawn (list)", 2, game.getDrawingsUntilDate(dateAfterSecondNumber).size());
    }

    @Test
    public void getTimeToNextDrawing() {
        final Date firstDrawingDate = game.getFirstDrawingDate();
        assertNotNull(firstDrawingDate);
        Date timeToNextDrawing = game.getTimeToNextDrawing(firstDrawingDate);
        assertEquals("on first drawing is next drawing", firstDrawingDate, timeToNextDrawing);

        final int drawingIntervalInSeconds = game.getDrawingIntervalInSeconds();
        assertTrue(drawingIntervalInSeconds > 0);
        timeToNextDrawing = game.getTimeToNextDrawing(DateUtils.addSeconds(firstDrawingDate, drawingIntervalInSeconds - 1));
        assertTrue(timeToNextDrawing.equals(DateUtils.addSeconds(firstDrawingDate, drawingIntervalInSeconds)));
    }

    @Test
    public void getDrawingsUntilDateAfterGame() {
        final Date date = DateUtils.addYears(game.getFirstDrawingDate(), 1);
        final List<Integer> drawingsUntilDate = game.getDrawingsUntilDate(date);
        assertEquals("exact 75 numbers are drawn", BingoGame.NUMBERS_IN_GAME, drawingsUntilDate.size());
    }

    @Test
    public void getDrawingEventStream() throws ExecutionException, TimeoutException, InterruptedException {
        game = new BingoGame();
        game.setDrawingIntervalInSeconds(1);
        game.setInitialWaitingTimeInSeconds(1);
        game.setStarted(true);
        game.save();
        final List<Integer> drawingsUntilNow = game.getDrawingsUntilNow();
        final int sizeBefore = drawingsUntilNow.size();
        F.EventStream<String> drawingEventStream = game.getDrawingEventStream();
        final F.Promise<String> promise = drawingEventStream.nextEvent();
        promise.get(4, TimeUnit.SECONDS);
        assertEquals(sizeBefore + 1, game.getDrawingsUntilNow().size());
    }

    @Test
    public void getDateForDrawing() {
        final int expectedNumberOfDrawnNumbers = 4;
        final Date dateForDrawing = game.getDateForDrawing(expectedNumberOfDrawnNumbers);
        final int actualNumberOfDrawnNumbers = game.numberOfDrawnNumbersUntil(dateForDrawing);
        assertEquals(actualNumberOfDrawnNumbers, expectedNumberOfDrawnNumbers);
    }

}
