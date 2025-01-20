package dev.osunolimits.main;

import java.sql.SQLException;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.stripe.Stripe;

import ch.qos.logback.classic.Logger;
import dev.osunolimits.models.Action;
import dev.osunolimits.modules.ShiinaDocs;
import dev.osunolimits.modules.ShiinaRankCache;
import dev.osunolimits.modules.utils.GroupRegistry;
import dev.osunolimits.modules.utils.RobotJsonConfig;
import dev.osunolimits.modules.utils.ThemeLoader;
import dev.osunolimits.modules.utils.UserInfoCache;
import dev.osunolimits.monetization.MonetizationConfig;
import dev.osunolimits.monetization.StripeMethod;
import dev.osunolimits.plugins.PluginLoader;
import dev.osunolimits.routes.ap.api.PubSubHandler;
import dev.osunolimits.routes.ap.api.RecoverAccount;
import dev.osunolimits.routes.ap.get.Bancho;
import dev.osunolimits.routes.ap.get.Commands;
import dev.osunolimits.routes.ap.get.Multiaccounts;
import dev.osunolimits.routes.ap.get.Start;
import dev.osunolimits.routes.ap.get.Themes;
import dev.osunolimits.routes.ap.get.groups.Groups;
import dev.osunolimits.routes.ap.get.groups.ManageGroup;
import dev.osunolimits.routes.ap.get.groups.ProcessGroup;
import dev.osunolimits.routes.ap.get.system.System;
import dev.osunolimits.routes.ap.get.system.SystemConnections;
import dev.osunolimits.routes.ap.get.system.SystemThreads;
import dev.osunolimits.routes.ap.get.users.ApUser;
import dev.osunolimits.routes.ap.get.users.Users;
import dev.osunolimits.routes.ap.post.ChangeTheme;
import dev.osunolimits.routes.ap.post.ProcessManageGroup;
import dev.osunolimits.routes.api.get.GetBmThumbnail;
import dev.osunolimits.routes.api.get.GetFirstPlaces;
import dev.osunolimits.routes.api.get.GetPlayerScores;
import dev.osunolimits.routes.api.get.Search;
import dev.osunolimits.routes.api.get.auth.UpdateRelationship;
import dev.osunolimits.routes.get.Beatmap;
import dev.osunolimits.routes.get.Beatmaps;
import dev.osunolimits.routes.get.Clan;
import dev.osunolimits.routes.get.Clans;
import dev.osunolimits.routes.get.Home;
import dev.osunolimits.routes.get.Leaderboard;
import dev.osunolimits.routes.get.User;
import dev.osunolimits.routes.get.UserScore;
import dev.osunolimits.routes.get.errors.NotFound;
import dev.osunolimits.routes.get.simple.Donate;
import dev.osunolimits.routes.get.simple.Login;
import dev.osunolimits.routes.get.simple.Recover;
import dev.osunolimits.routes.get.simple.Register;
import dev.osunolimits.routes.get.user.Relations;
import dev.osunolimits.routes.get.user.Settings;
import dev.osunolimits.routes.post.HandleAvatarChange;
import dev.osunolimits.routes.post.HandleBannerChange;
import dev.osunolimits.routes.post.HandleDonate;
import dev.osunolimits.routes.post.HandleFlagChange;
import dev.osunolimits.routes.post.HandleLogin;
import dev.osunolimits.routes.post.HandleLogout;
import dev.osunolimits.routes.post.HandleNameChange;
import dev.osunolimits.routes.post.HandleRecovery;
import dev.osunolimits.routes.post.HandleRegister;
import dev.osunolimits.routes.post.HandleUserpageChange;
import dev.osunolimits.routes.redirects.GucchoBmRedirect;
import dev.osunolimits.routes.redirects.GucchoUserRedirect;
import io.github.cdimascio.dotenv.Dotenv;
import redis.clients.jedis.JedisPooled;
import spark.Spark;

/**
 * Shiina a attemp to make a modular bancho.py full stack application
 * By Marc Andre Herpers
 */
public class App {
    public static final Logger log = (Logger) LoggerFactory.getLogger(App.class);
    public static Dotenv loggerEnv;
    public static Dotenv env;
    public static Map<String, Object> customization;
    public static JedisPooled jedisPool;
    public static WebServer webServer;

    public static String version = "1.1rc1";
    public static String dbVersion = "1.0";

    public static void main(String[] args) throws SQLException {
        env = Dotenv.configure().directory(".config/").load();
        loggerEnv = Dotenv.configure().directory(".config/").filename("logger.env").load();
        log.info("RhythmForge-Web built on Shiina-Web Rewrite "+version);
        Init init = new Init();
        init.initializeDataDirectory();
        init.initializeRedisConfiguration();
        init.initializeJettyLogging();
        init.initializeDatabase();
        init.initializeConnectionWatcher();
        init.initializeAutorunSQL();
        init.initializeJedis();
        customization = init.initializeCustomizations();

        ThemeLoader.loadThemes();

        webServer = new WebServer();
        init.initializeWebServer(webServer);
        init.initializeOkHttpCacheReset();

        RobotJsonConfig robotJsonConfig = new RobotJsonConfig();
        robotJsonConfig.updateRobotsTxt();

        WebServer.get("/user/:handle", new GucchoUserRedirect());
        WebServer.get("/beatmapset/:set", new GucchoBmRedirect());

        WebServer.get("/", new Home());
        WebServer.get("/beatmaps", new Beatmaps());
        WebServer.get("/leaderboard", new Leaderboard());
        WebServer.get("/clans", new Clans());
        WebServer.get("/clan/:id", new Clan());
        WebServer.get("/scores/:id", new UserScore());
        WebServer.get("/b/:id", new Beatmap());
        WebServer.get("/u/:id", new User());
        WebServer.get("/settings", new Settings());
        WebServer.get("/friends", new Relations());
        WebServer.post("/settings/avatar", new HandleAvatarChange());
        WebServer.post("/settings/banner", new HandleBannerChange());
        WebServer.post("/settings/country", new HandleFlagChange());
        WebServer.post("/settings/name", new HandleNameChange());
        WebServer.post("/settings/userpage", new HandleUserpageChange());
        WebServer.get("/login", new Login());
        WebServer.get("/register", new Register());
        WebServer.get("/auth/recover", new Recover());
        WebServer.post("/recover", new HandleRecovery());
        WebServer.post("/register", new HandleRegister());
        WebServer.post("/login", new HandleLogin());
        WebServer.post("/logout", new HandleLogout());
        WebServer.notFound(new NotFound());

        WebServer.get("/api/v1/get_first_places", new GetFirstPlaces());
        WebServer.get("/api/v1/get_player_scores", new GetPlayerScores());
        WebServer.get("/api/v1/search", new Search());
        WebServer.get("/api/v1/thumb", new GetBmThumbnail());

        WebServer.get("/api/v1/update_rel", new UpdateRelationship());

        WebServer.get("/ap/users/recovery", new RecoverAccount());
        
        WebServer.get("/ap/bancho", new Bancho());
        WebServer.get("/ap/api/handler", new PubSubHandler());

        WebServer.get("/ap/multiaccs", new Multiaccounts());
        WebServer.get("/ap/start", new Start());
        WebServer.get("/ap/commands", new Commands());
        WebServer.get("/ap/themes", new Themes());
        WebServer.get("/ap/system", new System());
        WebServer.get("/ap/system/cons", new SystemConnections());
        WebServer.get("/ap/system/threads", new SystemThreads());
        WebServer.get("/ap/groups", new Groups());
        WebServer.get("/ap/groups/create", new ManageGroup(Action.CREATE));
        WebServer.get("/ap/groups/edit", new ManageGroup(Action.EDIT));
        WebServer.get("/ap/groups/delete", new ManageGroup(Action.DELETE));
        WebServer.post("/ap/groups/manage", new ProcessManageGroup());
        WebServer.get("/ap/groups/process", new ProcessGroup());
        WebServer.get("/ap/users", new Users());
        WebServer.get("/ap/user", new ApUser());
        WebServer.post("/ap/themes/change", new ChangeTheme());

        GroupRegistry.revalidate();

        UserInfoCache userInfoCache = new UserInfoCache();
        userInfoCache.populateIfNeeded();

        MonetizationConfig config = new MonetizationConfig();
        if(config.ENABLED) {
            StripeMethod.registerWebhookRoute(config);
       
            Stripe.apiKey = config.getStripeConfig().getClientSecret();
            Spark.post("/donate", new HandleDonate());
            Spark.get("/donate", new Donate(config));
        }

        ShiinaDocs shiinaDocs = new ShiinaDocs();
        shiinaDocs.initializeDocs();

        PluginLoader pluginLoader = new PluginLoader();
        pluginLoader.loadPlugins();

        new ShiinaRankCache();

        try {
            shiinaDocs.watchDirectory();
        } catch (Exception e) {
            log.error("Error while running docs", e);
        }

    }

    

}
