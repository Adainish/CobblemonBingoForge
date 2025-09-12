package io.github.adainish.cobblemonbingo.conf;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.github.adainish.cobblemonbingo.CobblemonBingo;
import io.github.adainish.cobblemonbingo.manager.RewardManager;
import io.github.adainish.cobblemonbingo.util.Adapters;

import java.io.*;

public class RewardsConfig
{
    public RewardManager rewardManager;


    public RewardsConfig()
    {
        this.rewardManager = new RewardManager();
    }

    public static void writeConfig()
    {
        File dir = CobblemonBingo.getConfigDir();
        dir.mkdirs();
        Gson gson  = Adapters.PRETTY_MAIN_GSON;
        RewardsConfig config = new RewardsConfig();
        try {
            File file = new File(dir, "rewards.json");
            if (file.exists())
                return;
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            String json = gson.toJson(config);
            writer.write(json);
            writer.close();
        } catch (IOException e)
        {
            CobblemonBingo.getLog().warn(e);
        }
    }

    public static void saveConfig(RewardsConfig config)
    {
        File dir = CobblemonBingo.getConfigDir();
        dir.mkdirs();
        File file = new File(dir, "rewards.json");
        Gson gson = Adapters.PRETTY_MAIN_GSON;
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (reader == null) {
            CobblemonBingo.getLog().error("Something went wrong attempting to read the Rewards Config");
            return;
        }


        try {
            FileWriter writer = new FileWriter(file);
            writer.write(gson.toJson(config));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static RewardsConfig getConfig()
    {
        File dir = CobblemonBingo.getConfigDir();
        dir.mkdirs();
        Gson gson  = Adapters.PRETTY_MAIN_GSON;
        File file = new File(dir, "rewards.json");
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            CobblemonBingo.getLog().error("Something went wrong attempting to read the Rewards Config");
            return null;
        }

        return gson.fromJson(reader, RewardsConfig.class);
    }
}
