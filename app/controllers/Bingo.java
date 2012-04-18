package controllers;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import exceptions.BingoException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.BingoGame;
import models.Coupon;
import models.User;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.Max;
import play.data.validation.Min;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.i18n.Messages;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.WebSocketController;
import play.mvc.With;
import play.templates.Template;
import play.templates.TemplateLoader;

@With(Secure.class)
public class Bingo extends Controller {

    private static final int MIN_COUPONS = 1;
    private static final int MAX_COUPONS = 6;

    public static void buyCoupons(@Required @Min(MIN_COUPONS)
                                  @Max(MAX_COUPONS) int count) {
        if (validation.hasErrors()) {
            render("Bingo/buyCouponsForm.html", count);
        } else {
            User user = Security.getCurrentUser();
            user.buyCoupons(count);
            user.save();
            render(count);
        }
    }


    public static class WebSocketFallback extends Controller {
        public static void drawing(@Required long gameId) {
            BingoGame game = BingoGame.findById(gameId);
            if (game == null) {
                notFound();
            } else {
                renderText(new Gson().toJson(game.getDrawingsUntilNow()));
            }
        }
    }

    public static class WebSocket extends WebSocketController {
        public static void drawing(@Required long gameId) {
            BingoGame game = BingoGame.findById(gameId);
            if (game == null) {
                notFound();
            } else {
                final F.EventStream<String> drawingEventStream = game.getDrawingEventStream();
                while (inbound.isOpen()) {
                    await(drawingEventStream.nextEvent());//await clears JPA session
                    game = BingoGame.findById(gameId);
                    final String jsonData = new Gson().toJson(game.getDrawingsUntilNow());
                    if (outbound.isOpen()) {
                        outbound.send(jsonData);
                    }
                }
            }
        }
    }

    public static void openGame(@Required @Valid BingoGame game) {
        if (validation.hasErrors()) {
            render("Bingo/openGameForm.html", game);
        } else {
            User currentUser = fetchCurrentUser();
            game.setOwner(currentUser);
            game.save();
            render(game);
        }
    }

    public static void buyCouponsForm() {
        render();
    }

    public static void openGameForm() {
        render();
    }

    private static User fetchCurrentUser() {
        return User.find("username", Security.connected()).first();
    }

    public static void game(@Required long gameId) {
        if (validation.hasErrors()) {
            notFound();
        } else {
            BingoGame game = BingoGame.findById(gameId);
            if (game == null || (!game.isStarted() && !gameOwnerIsCurrentUser(game))) {
                notFound();
            } else {
                if (!game.isStarted()) {
                    game.setStarted(true);
                    game.save();
                }
                final BingoGame.ClientInfo gameInfo = game.toClientInfo();
                final String gameInfoJson = new Gson().toJson(gameInfo);
                final List<Coupon> coupons = game.getCouponsForUserWithUsername(Security.connected());
                render(gameInfoJson, gameInfo, coupons);
            }
        }
    }


    public static void ajaxAddCoupon(@Required long gameId) {
        if (validation.hasErrors()) {
            notFound();
        }
        BingoGame game = BingoGame.findById(gameId);
        if (game == null) {
            notFound();
        }
        final User currentUser = fetchCurrentUser();

        final StringWriter out = new StringWriter();
        JsonWriter writer = new JsonWriter(out);
        try {
            try {
                final Coupon coupon = currentUser.useUnusedCouponForGame(game);
                Template templatePlay = TemplateLoader.load("tags/Bingo/coupon75.html");
                Map<String, Object> renderArgs = new HashMap<String, Object>();
                renderArgs.put("_coupon", coupon);
                final String renderedCoupon = templatePlay.render(renderArgs);
                writer.beginObject();
                writer.name("couponHtml").value(renderedCoupon);
                writer.endObject();
            } catch (BingoException e) {
                writer.beginObject();
                final String translatedErrorMessage = Messages.get(e.getMessage());
                writer.name("errorMessage").value(translatedErrorMessage);
                writer.endObject();
            }
            writer.close();
        } catch (IOException e) {
            Logger.error(e.toString());
            error();
        }
        renderText(out.toString());
    }

    private static boolean gameOwnerIsCurrentUser(BingoGame game) {
        return StringUtils.equals(game.getOwner().getUsername(), Security.connected());
    }

    public static void markCoupon(@Required boolean marked, @Required long couponId, @Required int number) {
        if (validation.hasErrors()) {
            notFound();
        } else {
            final Coupon coupon = Coupon.findById(couponId);
            final boolean couponExists = coupon != null;
            final boolean couponBelongsToCurrentUser = coupon.getOwner().getUsername().equals(Security.connected());
            if (couponExists && couponBelongsToCurrentUser) {
                coupon.setMarked(number, marked);
                coupon.save();
                ok();
            } else {
                notFound();
            }
        }
    }

    public static void callBingo(Long gameId) {
        renderJSON(BingoGame.callBingoForGame(Security.connected(), gameId));
    }

    public static void listGames() {
        List<BingoGame> gameList = BingoGame.findAll();
        render(gameList);
    }
}
