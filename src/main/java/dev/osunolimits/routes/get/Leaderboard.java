package dev.osunolimits.routes.get;

import java.util.Optional;

import dev.osunolimits.api.LeaderboardQuery;
import dev.osunolimits.api.LeaderboardQuery.LeaderboardResponse;
import dev.osunolimits.cache.CountryLeaderboardCache;
import dev.osunolimits.main.App;
import dev.osunolimits.modules.SEOBuilder;
import dev.osunolimits.modules.Shiina;
import dev.osunolimits.modules.ShiinaRoute;
import dev.osunolimits.modules.ShiinaRoute.ShiinaRequest;
import dev.osunolimits.utils.Validation;
import dev.osunolimits.utils.osu.OsuConverter;
import spark.Request;
import spark.Response;

public class Leaderboard extends Shiina {

    @Override
    public Object handle(Request req, Response res) throws Exception {
        ShiinaRequest shiina = new ShiinaRoute().handle(req, res);
        shiina.data.put("actNav", 2);

        LeaderboardQuery leaderboardQuery = new LeaderboardQuery();

        int page = 1;
        int offset = 0;
        if (req.queryParams("page") != null && Validation.isNumeric(req.queryParams("page"))) {
            page = Integer.parseInt(req.queryParams("page"));
        }
        if(page == 1) {
            offset = 0;
        } else {
            offset = (page - 1) * 50;
        }

        int mode = 0;
        if (req.queryParams("mode") != null && Validation.isNumeric(req.queryParams("mode"))) {
            mode = Integer.parseInt(req.queryParams("mode"));
        }

        Optional<String> country;
        if (req.queryParams("country") != null) {
            country = Optional.of(req.queryParams("country"));
        } else {
            country = Optional.empty();
        }

        String sort = "pp";
        if (req.queryParams("sort") != null) {
            sort = req.queryParams("sort");
        }

        LeaderboardResponse leaderboardResponse = leaderboardQuery.getLeaderboard(sort, mode, 50, offset, country);

        shiina.data.put("countries", CountryLeaderboardCache.getOrPut(mode, shiina.mysql));
        shiina.data.put("leaderboard", leaderboardResponse.getLeaderboard());

        shiina.data.put("seo", new SEOBuilder("Leaderboard | " + OsuConverter.convertModeBack(String.valueOf(mode)) + " | " + OsuConverter.SortHelper.convertSortBack(sort), App.customization.get("homeDescription").toString()));
        shiina.data.put("sort", sort);
        shiina.data.put("mode", mode);
        shiina.data.put("offset", offset + 1);
        shiina.data.put("country", country.orElse(null));
        shiina.data.put("page", page);
        shiina.data.put("pageSize", leaderboardResponse.getLeaderboard().length);
        return renderTemplate("leaderboard.html", shiina, res, req);
    }

}
