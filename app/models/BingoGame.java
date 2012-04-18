package models;

import jobs.BingoConferencier;
import models.chat.ChatRoom;
import org.apache.commons.lang.time.DateUtils;
import play.Play;
import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.F;
import util.ListUtils;

import javax.persistence.*;
import java.util.*;

@Entity
public class BingoGame extends Model {

    public static final int NUMBERS_IN_GAME = 75;

    private static Map<Long, F.EventStream> gameIdToDrawingEventStream = Collections.synchronizedMap(new HashMap<Long, F.EventStream>());

    public static final List<Boolean> winningPattern = Arrays.asList(
            true, false, false, false, true,
            true, true, false, true, true,
            true, false, /* empty */ false, true,
            true, false, false, false, true,
            true, false, false, false, true);

    @Required
    @Min(0)
    private int initialWaitingTimeInSeconds;

    @Required
    @Min(1)
    private int drawingIntervalInSeconds;

    @Required
    private String title;

    private boolean started = false;

    private boolean finished = false;

    @OneToOne
    private User owner;

    @OneToOne
    private User winner;

    private Date startingTime;

    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    private List<Integer> drawings;

    @OneToMany(mappedBy = "game")
    private List<Coupon> coupons;

    @OneToOne(cascade = CascadeType.ALL)
    private ChatRoom chatRoom = new ChatRoom();

    public BingoGame() {
        initialWaitingTimeInSeconds = Integer.parseInt((String) Play.configuration.get("bingo.game.initialWaitingTimeInSeconds.default"));
        drawingIntervalInSeconds = Integer.parseInt((String) Play.configuration.get("bingo.game.drawingIntervalInSeconds.default"));
        drawings = ListUtils.createListWithNumbersFromTo(1, NUMBERS_IN_GAME);
        Collections.shuffle(drawings);
    }

    public int getInitialWaitingTimeInSeconds() {
        return initialWaitingTimeInSeconds;
    }

    public void setInitialWaitingTimeInSeconds(int initialWaitingTimeInSeconds) {
        this.initialWaitingTimeInSeconds = initialWaitingTimeInSeconds;
    }

    public int getDrawingIntervalInSeconds() {
        return drawingIntervalInSeconds;
    }

    public void setDrawingIntervalInSeconds(int drawingIntervalInSeconds) {
        this.drawingIntervalInSeconds = drawingIntervalInSeconds;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        if (!isStarted()) {
            setStartingTime(new Date());
        }
        this.started = started;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public ClientInfo toClientInfo() {
        return new ClientInfo(this, new Date());
    }

    public ClientInfo toClientInfo(Date date) {
        return new ClientInfo(this, date);
    }

    public Date getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(Date startingTime) {
        this.startingTime = startingTime;
    }

    public List<Integer> getDrawings() {
        return drawings;
    }

    public void setDrawings(List<Integer> drawings) {
        this.drawings = drawings;
    }

    public F.EventStream<String> getDrawingEventStream() {
        if (gameIdToDrawingEventStream.get(id) == null) {
            synchronized (gameIdToDrawingEventStream) {
                F.EventStream<String> eventStream = new F.EventStream<String>();
                gameIdToDrawingEventStream.put(id, eventStream);
                new BingoConferencier(getId()).now();
            }
        }
        return gameIdToDrawingEventStream.get(id);//event stream in static var => each node has independent copy of event stream => share nothing
    }

    public Date getTimeToNextDrawing(Date currentTime) {
        int drawingNumber = getIndexNextDrawing(currentTime);
        Date result = getFirstDrawingDate();
        if (drawingNumber > 0) {
            result = DateUtils.addSeconds(getFirstDrawingDate(), getDrawingIntervalInSeconds() * drawingNumber);
        }
        return result;
    }

    private int getIndexNextDrawing(Date currentTime) {
        int index = 0;
        if (dateIsAfterFirstDrawing(currentTime)) {
            final long millisecondsSinceFirstDrawing = currentTime.getTime() - getFirstDrawingDate().getTime();
            final int millisecondsPerSecond = 1000;
            index = (int) Math.ceil(1.0f * millisecondsSinceFirstDrawing / (getDrawingIntervalInSeconds() * millisecondsPerSecond));
        }
        return index;
    }

    public Date getTimeToNextDrawing() {
        return getTimeToNextDrawing(new Date());
    }

    public List<Coupon> getCouponsForUserWithUsername(String username) {
        final List<Coupon> couponList = Coupon.find("game.id = ? and owner.username = ?", getId(), username).fetch();
        return couponList;
    }

    public boolean userCouldBeWinner(String username) {
        return userCouldBeWinner(username, new Date());
    }

    public boolean userCouldBeWinner(String username, Date currentTime) {
        boolean userCouldBeWinner = false;
        final List<Coupon> coupons = getCouponsForUserWithUsername(username);
        for (int couponIndex = 0; !userCouldBeWinner && couponIndex < coupons.size(); couponIndex++) {
            final Coupon coupon = coupons.get(couponIndex);
            userCouldBeWinner = couponMatchesPattern(coupon, currentTime);
        }
        return userCouldBeWinner;
    }


    private boolean couponMatchesPattern(Coupon coupon, Date currentTime) {
        final List<Integer> drawingsUntilDate = getDrawingsUntilDate(currentTime);
        final List<Integer> couponNumbers = coupon.getNumbersAsList();
        final int size = couponNumbers.size();
        boolean couponMatchesPattern = drawingsUntilDate.size() >= getNumberOfRequiredCorrectNumbers();
        for (int i = 0; couponMatchesPattern && i < size; i++) {
            final Boolean currentPositionMattersForWinning = winningPattern.get(i);
            if (currentPositionMattersForWinning) {
                final Integer number = couponNumbers.get(i);
                couponMatchesPattern = drawingsUntilDate.indexOf(number) >= 0;
            }
        }
        return couponMatchesPattern;
    }

    public boolean callBingoForUser(String username) {
        final boolean isAlreadyWinner = winner != null && winner.getUsername().equals(username);
        boolean hasWon = isAlreadyWinner || (!isFinished() && userCouldBeWinner(username));
        if (hasWon && !isFinished()) {
            setFinished(true);
            winner = User.find("username", username).first();
            save();
        }
        return hasWon;
    }

    public static CallBingoResult callBingoForGame(String username, Long gameId) {
        BingoGame game = BingoGame.findById(gameId);
        game.em().lock(game, LockModeType.PESSIMISTIC_WRITE);
        CallBingoResult result = new CallBingoResult(false, "bingo.game.NotFound");
        if (game != null) {
            final boolean hasWon = game.callBingoForUser(username);
            if (hasWon) {
                result = new CallBingoResult(hasWon, "bingo.game.YouHaveWon");
            } else {
                result = new CallBingoResult(hasWon, "bingo.game.YouHaveNotWon");
            }
        }
        return result;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }


    public static class ClientInfo {
        private final int initialWaitingTimeInSeconds;
        private final int drawingIntervalInSeconds;
        private final String title;
        private final boolean started;
        private final Date startingTime;
        private final Date firstDrawing;
        private final long id;
        private final List<Integer> drawings;
        private final long chatRoomId;

        protected ClientInfo(BingoGame game, Date date) {
            initialWaitingTimeInSeconds = game.getInitialWaitingTimeInSeconds();
            drawingIntervalInSeconds = game.getDrawingIntervalInSeconds();
            title = game.getTitle();
            started = game.isStarted();
            startingTime = game.getStartingTime();
            firstDrawing = game.getFirstDrawingDate();
            id = game.getId();
            drawings = game.getDrawingsUntilDate(date);
            chatRoomId = game.getChatRoom().getId();
        }

        public int getInitialWaitingTimeInSeconds() {
            return initialWaitingTimeInSeconds;
        }

        public int getDrawingIntervalInSeconds() {
            return drawingIntervalInSeconds;
        }

        public String getTitle() {
            return title;
        }

        public boolean isStarted() {
            return started;
        }

        public Date getStartingTime() {
            return startingTime;
        }

        public Date getFirstDrawing() {
            return firstDrawing;
        }

        public long getId() {
            return id;
        }

        public List<Integer> getDrawings() {
            return drawings;
        }

        public long getChatRoomId() {
            return chatRoomId;
        }
    }

    public Date getFirstDrawingDate() {
        return DateUtils.addSeconds(getStartingTime(), getInitialWaitingTimeInSeconds());
    }

    public List<Integer> getDrawingsUntilDate(Date date) {
        int drawingNumber = numberOfDrawnNumbersUntil(date);
        List<Integer> result = Collections.emptyList();
        if (drawingNumber > 0) {
            final int lastIndex = drawingNumber >= NUMBERS_IN_GAME ? NUMBERS_IN_GAME : drawingNumber;
            result = getDrawings().subList(0, lastIndex);
        }
        return result;
    }

    public List<Integer> getDrawingsUntilNow() {
        return getDrawingsUntilDate(new Date());
    }

    public int numberOfDrawnNumbersUntil(Date date) {
        int drawingNumber = 0;
        if (dateIsAfterFirstDrawing(date)) {
            final long millisecondsSinceFirstDrawing = date.getTime() - getFirstDrawingDate().getTime();
            final int millisecondsPerSecond = 1000;
            drawingNumber = 1 + (int) Math.floor(1.0f * millisecondsSinceFirstDrawing / (getDrawingIntervalInSeconds() * millisecondsPerSecond));
        } else if (getFirstDrawingDate().equals(date)) {
            drawingNumber = 1;
        }
        return drawingNumber;
    }

    public Date getDateForDrawing(int drawing) {
        if (drawing < 0) {
            throw new IllegalArgumentException("drawing number should be equal or greater than 0");
        }
        Date date = getFirstDrawingDate();
        while (numberOfDrawnNumbersUntil(date) < drawing) {
            date = getTimeToNextDrawing(DateUtils.addMilliseconds(date, 1));
        }
        return date;
    }

    public boolean dateIsAfterFirstDrawing(Date date) {
        return date.compareTo(getFirstDrawingDate()) > 0;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getNumberOfRequiredCorrectNumbers() {
        int numberOfRequiredCorrectNumbers = 0;
        for (Boolean isRequired : winningPattern) {
            if (isRequired) {
                numberOfRequiredCorrectNumbers++;
            }
        }
        return numberOfRequiredCorrectNumbers;
    }
}