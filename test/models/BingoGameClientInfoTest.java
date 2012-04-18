package models;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import play.test.UnitTest;

import java.util.Arrays;
import java.util.Date;

public class BingoGameClientInfoTest extends UnitTest {

    @Test
    public void json() {
        final String expectedJsonStart = "\"initialWaitingTimeInSeconds\":10,\"drawingIntervalInSeconds\":4,\"title\":\"Test\",\"started\":true,\"startingTime\":\"";
        final String expectedJsonEndBeforeFirstDrawing = "\"drawings\":[]";
        final String expectedJsonEndAfterFirstDrawing = "\"drawings\":[21,49,15,63,44]";
        final BingoGame game = new BingoGame();
        game.setOwner(User.find("username", "Michael").<User>first());
        final int initialWaitingTimeInSeconds = 10;
        game.setInitialWaitingTimeInSeconds(initialWaitingTimeInSeconds);
        final int drawingIntervalInSeconds = 4;
        game.setDrawingIntervalInSeconds(drawingIntervalInSeconds);
        game.setTitle("Test");
        game.setStarted(true);
        game.setDrawings(Arrays.asList(21, 49, 15, 63, 44, 55));
        game.save();
        final Date startingTime = game.getStartingTime();
        final Date firstDrawingDate = game.getFirstDrawingDate();
        final Date beforeFirstDrawing = DateUtils.addSeconds(startingTime, initialWaitingTimeInSeconds - 2);
        assertTrue(firstDrawingDate.getTime() - beforeFirstDrawing.getTime() > 0);
        final BingoGame.ClientInfo clientInfo = game.toClientInfo(beforeFirstDrawing);
        String actualJson = new Gson().toJson(clientInfo);
        assertTrue("start matches", StringUtils.contains(actualJson, expectedJsonStart));
        assertTrue("no drawn numbers present", StringUtils.contains(actualJson, expectedJsonEndBeforeFirstDrawing));

        Date afterFourthDrawing = DateUtils.addSeconds(game.getFirstDrawingDate(), +5 * drawingIntervalInSeconds - 1);
        actualJson = new Gson().toJson(game.toClientInfo(afterFourthDrawing));
        assertTrue("start still matches", StringUtils.contains(actualJson, expectedJsonStart));
        assertTrue("drawn numbers present", StringUtils.contains(actualJson, expectedJsonEndAfterFirstDrawing));
    }
}
