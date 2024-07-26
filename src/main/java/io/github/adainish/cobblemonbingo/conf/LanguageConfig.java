package io.github.adainish.cobblemonbingo.conf;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.github.adainish.cobblemonbingo.CobblemonBingo;
import io.github.adainish.cobblemonbingo.util.Adapters;

import java.io.*;

public class LanguageConfig
{

    public String prefix;
    public String splitter;

    public LanguageConfig()
    {
        this.prefix = "&6[&bBingo&6]";
        this.splitter = " &e» ";
    }

    public static void writeConfig()
    {
        File dir = CobblemonBingo.getConfigDir();
        dir.mkdirs();
        Gson gson  = Adapters.PRETTY_MAIN_GSON;
        LanguageConfig config = new LanguageConfig();
        try {
            File file = new File(dir, "language.json");
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

    public static void saveConfig(LanguageConfig config)
    {
        File dir = CobblemonBingo.getConfigDir();
        dir.mkdirs();
        File file = new File(dir, "language.json");
        Gson gson = Adapters.PRETTY_MAIN_GSON;
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (reader == null) {
            CobblemonBingo.getLog().error("Something went wrong attempting to read the Language Config");
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

    public static LanguageConfig getConfig()
    {
        File dir = CobblemonBingo.getConfigDir();
        dir.mkdirs();
        Gson gson  = Adapters.PRETTY_MAIN_GSON;
        File file = new File(dir, "language.json");
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            CobblemonBingo.getLog().error("Something went wrong attempting to read the Language Config");
            return null;
        }

        return gson.fromJson(reader, LanguageConfig.class);
    }
}
