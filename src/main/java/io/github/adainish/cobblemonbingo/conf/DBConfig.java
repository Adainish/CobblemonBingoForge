package io.github.adainish.cobblemonbingo.conf;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.github.adainish.cobblemonbingo.CobblemonBingo;
import io.github.adainish.cobblemonbingo.util.Adapters;

import java.io.*;

public class DBConfig
{
    public boolean enabled = false;
    public String mongoDBURI = "";
    public String database = "mydatabase";
    public String tableName = "player_data";

    public DBConfig()
    {

    }


    public static void writeConfig()
    {
        File dir = CobblemonBingo.getConfigDir();
        dir.mkdirs();
        Gson gson  = Adapters.PRETTY_MAIN_GSON;
        DBConfig config = new DBConfig();
        try {
            File file = new File(dir, "db_settings.json");
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

    public static DBConfig getConfig()
    {
        File dir = CobblemonBingo.getConfigDir();
        dir.mkdirs();
        Gson gson  = Adapters.PRETTY_MAIN_GSON;
        File file = new File(dir, "db_settings.json");
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            CobblemonBingo.getLog().error("Something went wrong attempting to read the Config");
            return null;
        }

        return gson.fromJson(reader, DBConfig.class);
    }
}
