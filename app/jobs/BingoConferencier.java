package jobs;

import models.BingoGame;
import play.Logger;
import play.jobs.Job;
import play.libs.F;

import java.util.Date;

public class BingoConferencier extends Job {
    private final BingoGame game;

    public BingoConferencier(long gameId) {
        game = BingoGame.findById(gameId);
    }

    public void doJob() {
        final F.EventStream drawingEventStream = game.getDrawingEventStream();
        boolean jobShouldRun = !game.isFinished();
        while (jobShouldRun) {
            final Date dateNextDrawing = game.getTimeToNextDrawing();
            final long nextDrawing = dateNextDrawing.getTime();
            final long millisecondsToNextDrawing = (nextDrawing - new Date().getTime());
            if (millisecondsToNextDrawing > 0) {
                try {
                    Thread.sleep(millisecondsToNextDrawing);
                    drawingEventStream.publish("HERE!");
                    //            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
//            Logger.info("first: " + simpleDateFormat.format(game.getFirstDrawingDate()));
//            Logger.info("now: " + simpleDateFormat.format(new Date()));
//            Logger.info("expected: " + simpleDateFormat.format(nextDrawing));
//            Logger.info("-------------");
                    jobShouldRun = !game.isFinished();
                } catch (InterruptedException e) {
                    Logger.warn("BingoConferencier stopped", e);
                    jobShouldRun = false;
                }
            }
        }
    }
}
