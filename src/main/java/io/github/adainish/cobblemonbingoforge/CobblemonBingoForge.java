package io.github.adainish.cobblemonbingoforge;

import ca.landonjw.gooeylibs2.api.tasks.Task;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.platform.events.PlatformEvents;
import io.github.adainish.cobblemonbingoforge.cmd.Command;
import io.github.adainish.cobblemonbingoforge.conf.Config;
import io.github.adainish.cobblemonbingoforge.conf.DBConfig;
import io.github.adainish.cobblemonbingoforge.conf.LanguageConfig;
import io.github.adainish.cobblemonbingoforge.conf.RewardsConfig;
import io.github.adainish.cobblemonbingoforge.obj.Player;
import io.github.adainish.cobblemonbingoforge.storage.Database;
import io.github.adainish.cobblemonbingoforge.storage.PlayerStorage;
import io.github.adainish.cobblemonbingoforge.subscriptions.EventSubscriptions;
import io.github.adainish.cobblemonbingoforge.tasks.SavePlayerTask;
import io.github.adainish.cobblemonbingoforge.wrapper.DataWrapper;
import kotlin.Unit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file

public class CobblemonBingoForge implements ModInitializer {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "cobblemonbingoforge";

    public static CobblemonBingoForge instance;
    public static final String MOD_NAME = "CobblemonBingo";
    public static final String VERSION = "1.1.0-Beta";
    public static final String AUTHORS = "Winglet";
    public static final String YEAR = "2023";
    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(MOD_NAME);
    private static File configDir;
    private static File storageDir;

    private static File playerStorageDir;
    private static MinecraftServer server;


    public static Logger getLog() {
        return log;
    }

    public static MinecraftServer getServer() {
        return server;
    }

    public static void setServer(MinecraftServer server) {
        CobblemonBingoForge.server = server;
    }

    public static File getConfigDir() {
        return configDir;
    }

    public static void setConfigDir(File configDir) {
        CobblemonBingoForge.configDir = configDir;
    }

    public static File getPlayerStorageDir() {
        return playerStorageDir;
    }

    public static void setPlayerStorageDir(File playerStorageDir) {
        CobblemonBingoForge.playerStorageDir = playerStorageDir;
    }

    public static Config config;
    public static LanguageConfig languageConfig;

    public static RewardsConfig rewardsConfig;
    public static DBConfig dbConfig;
    public static EventSubscriptions subscriptions;

    public static DataWrapper wrapper;
    public static PlayerStorage playerStorage;

    // Directly reference a slf4j logger
    public CobblemonBingoForge() {
        instance = this;
    }

    @Override
    public void onInitialize() {
        this.commonSetup();
    }

    private void commonSetup() {
        log.info("Booting up %n by %authors %v %y"
                .replace("%n", MOD_NAME)
                .replace("%authors", AUTHORS)
                .replace("%v", VERSION)
                .replace("%y", YEAR)
        );

        PlatformEvents.SERVER_STARTED.subscribe(Priority.NORMAL, t -> {
            setServer(t.getServer());
            wrapper = new DataWrapper();
            playerStorage = new PlayerStorage();
            //load data from config
            reload();
            if (dbConfig != null)
            {
                if (dbConfig.enabled)
                {
                    playerStorage.database = new Database();
                }
            }
            //register listeners
            subscriptions = new EventSubscriptions();
            Task.builder().interval(20 * 60).execute(new SavePlayerTask()).infinite().build();
            return Unit.INSTANCE;
        });

        PlatformEvents.SERVER_STOPPING.subscribe(Priority.NORMAL, t -> {
            this.handleShutDown();
            return Unit.INSTANCE;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryaccess, environment) -> {
            dispatcher.register(Command.getCommand());
        });
    }





    public void initDirs() {
        setConfigDir(new File(FabricLoader.getInstance().getConfigDir()  + "/CobblemonBingo/"));
        getConfigDir().mkdir();
        setPlayerStorageDir(new File(configDir, "/playerdata/"));
        getPlayerStorageDir().mkdirs();
    }


    public void save()
    {
        if (config != null)
            Config.saveConfig(config);
        else log.warn("Failed to save");
    }

    public void initConfigs() {
        log.warn("Loading Config Files");
        Config.writeConfig();
        config = Config.getConfig();
        LanguageConfig.writeConfig();
        languageConfig = LanguageConfig.getConfig();
        RewardsConfig.writeConfig();
        rewardsConfig = RewardsConfig.getConfig();
        DBConfig.writeConfig();
        dbConfig = DBConfig.getConfig();
        log.warn("Loaded Config files");
    }

    public void reload() {
        initDirs();
        initConfigs();
    }

    public void handleShutDown()
    {
        playerStorage.saveAll();
        if (playerStorage.database != null)
            playerStorage.database.shutdown();
    }


}
