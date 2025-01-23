package dev.osunolimits.api;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

import dev.osunolimits.common.APIRequest;
import dev.osunolimits.main.App;
import dev.osunolimits.models.Group;
import dev.osunolimits.models.UserInfoObject;
import dev.osunolimits.utils.CacheInterceptor;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

public class MMLeaderboardQuery {

    private Gson gson;


    @Data
    public class MMLeaderboardResponse {
        private String status;
        private MMLeaderboardItem[] mm_leaderboard;
        //private MMLeaderboardResponse[] mm_leaderboard;
    }

    @Data
    public class MMLeaderboardItem {
        @SerializedName("player_id")
        private int playerId;

        @SerializedName("name")
        private String name;

        @SerializedName("country")
        private String country;

        @SerializedName("elo")
        private String elo;

        @SerializedName("clan_id")
        private Integer clanId;

        @SerializedName("clan_name")
        private String clanName;

        @SerializedName("clan_tag")
        private String clanTag;

        private List<Group> groups;
    }

    private OkHttpClient client;

    public MMLeaderboardQuery() {
        client = new OkHttpClient.Builder()
                .addNetworkInterceptor(new CacheInterceptor(5, TimeUnit.MINUTES))
                .cache(new Cache(new File(".cache/mm_leaderboard"), 100L * 1024L * 1024L))
                .connectionPool(new ConnectionPool(50, 20, TimeUnit.SECONDS)).build();
        gson = new Gson();
    }

    private int parameter = 0;

    public String getParameter() {
        if (parameter == 0) {
            parameter++;
            return "?";
        } else {
            return "&";
        }

    }

    public MMLeaderboardResponse getMMLeaderboard(String sort, int mode, int limit, int offset, @NotNull Optional<String> country) {
        String url = "/v1/get_mm_leaderboard";
        url += getParameter() + "sort=" + sort;
        url += getParameter() + "mode=" + mode;
        url += getParameter() + "limit=" + limit;
        url += getParameter() + "offset=" + offset;

        if (country.isPresent()) {
            url += getParameter() + "country=" + country.get();
        }

        Request request = APIRequest.build(url);
        
        try {
            Response response = client.newCall(request).execute();
            JsonElement element = JsonParser.parseString(response.body().string());
            MMLeaderboardResponse mmLeaderboardResponse = gson.fromJson(element, MMLeaderboardResponse.class);
            for (MMLeaderboardItem item : mmLeaderboardResponse.getMMLeaderboard()) {
                UserInfoObject userInfo = gson.fromJson(App.jedisPool.get("shiina:user:" + item.getPlayerId()),
                        UserInfoObject.class);
                if(userInfo != null) {
                    item.setGroups(userInfo.getGroups());
                }
            }
            return mmLeaderboardResponse;

        } catch (Exception e) {
            App.log.error("Failed to get Matchmaking Leaderboard", e);
            return null;
        }
    }
}
