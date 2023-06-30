package io.github.adainish.cobblemonbingoforge;

import ca.landonjw.gooeylibs2.api.tasks.Task;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.mojang.logging.LogUtils;
import io.github.adainish.cobblemonbingoforge.cmd.Command;
import io.github.adainish.cobblemonbingoforge.conf.Config;
import io.github.adainish.cobblemonbingoforge.conf.LanguageConfig;
import io.github.adainish.cobblemonbingoforge.conf.RewardsConfig;
import io.github.adainish.cobblemonbingoforge.obj.Player;
import io.github.adainish.cobblemonbingoforge.subscriptions.EventSubscriptions;
import io.github.adainish.cobblemonbingoforge.tasks.SavePlayerTask;
import io.github.adainish.cobblemonbingoforge.wrapper.DataWrapper;
import kotlin.Unit;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CobblemonBingoForge.MODID)
public class CobblemonBingoForge {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "cobblemonbingoforge";

    public static CobblemonBingoForge instance;
    public static final String MOD_NAME = "CobblemonBingo";
    public static final String VERSION = "1.0.0-Beta";
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

    public static EventSubscriptions subscriptions;

    public DataWrapper dataWrapper;

    // Directly reference a slf4j logger
    public CobblemonBingoForge() {
        instance = this;
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);


        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        log.info("Booting up %n by %authors %v %y"
                .replace("%n", MOD_NAME)
                .replace("%authors", AUTHORS)
                .replace("%v", VERSION)
                .replace("%y", YEAR)
        );

        CobblemonEvents.SERVER_STARTED.subscribe(Priority.NORMAL, minecraftServer -> {
            setServer(minecraftServer);
            dataWrapper = new DataWrapper();
            //load data from config
            reload();
            //register listeners
            subscriptions = new EventSubscriptions();
            Task.builder().interval(20 * 60).execute(new SavePlayerTask()).infinite().build();
            return Unit.INSTANCE;
        });

        CobblemonEvents.SERVER_STOPPING.subscribe(Priority.NORMAL, minecraftServer -> {
            List<Player> playerList = new ArrayList<>(CobblemonBingoForge.instance.dataWrapper.playerCache.values());
            playerList.forEach(player -> {
                player.expireOldCards();
                player.save();
            });
            return Unit.INSTANCE;
        });
    }



    @SubscribeEvent
    public void onCommandRegistration(RegisterCommandsEvent event)
    {
        event.getDispatcher().register(Command.getCommand());
    }


    public void initDirs() {
        setConfigDir(new File(FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()) + "/CobblemonBingo/"));
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
        log.warn("Loaded Config files");
    }

    public void reload() {
        initDirs();
        initConfigs();
    }


}
