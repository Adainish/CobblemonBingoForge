package io.github.adainish.cobblemonbingo.conf;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.github.adainish.cobblemonbingo.CobblemonBingo;
import io.github.adainish.cobblemonbingo.manager.BingoManager;
import io.github.adainish.cobblemonbingo.util.Adapters;

import java.io.*;

public class Config
{
    public BingoManager bingoManager;

    public Config()
    {
        this.bingoManager = new BingoManager();
    }

    public static void writeConfig()
    {
        File dir = CobblemonBingo.getConfigDir();
        dir.mkdirs();
        Gson gson  = Adapters.PRETTY_MAIN_GSON;
        Config config = new Config();
        try {
            File file = new File(dir, "config.json");
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

    public static void saveConfig(Config config)
    {
        File dir = CobblemonBingo.getConfigDir();
        dir.mkdirs();
        File file = new File(dir, "config.json");
        Gson gson = Adapters.PRETTY_MAIN_GSON;
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (reader == null) {
            CobblemonBingo.getLog().error("Something went wrong attempting to read the Config");
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

    public static Config getConfig()
    {
        File dir = CobblemonBingo.getConfigDir();
        dir.mkdirs();
        Gson gson  = Adapters.PRETTY_MAIN_GSON;
        File file = new File(dir, "config.json");
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            CobblemonBingo.getLog().error("Something went wrong attempting to read the Config");
            return null;
        }

        return gson.fromJson(reader, Config.class);
    }

}
