package io.github.adainish.cobblemonbingo.storage;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.github.adainish.cobblemonbingo.CobblemonBingo;
import io.github.adainish.cobblemonbingo.obj.Player;
import io.github.adainish.cobblemonbingo.util.Adapters;
import net.minecraft.server.level.ServerPlayer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerStorage
{
    public Database database;
    public void saveAll()
    {
        CobblemonBingo.getLog().warn("Saving player data...");
        CobblemonBingo.wrapper.playerCache.forEach((uuid, player) -> {
            player.saveNoCache();
        });
        CobblemonBingo.wrapper.playerCache.clear();
    }

    public Player getPlayerFlatFile(UUID uuid) {
        if (CobblemonBingo.wrapper.playerCache.containsKey(uuid))
            return CobblemonBingo.wrapper.playerCache.get(uuid);

        File dir = CobblemonBingo.getPlayerStorageDir();
        dir.mkdirs();


        File dataFile = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(uuid)));
        Gson gson = Adapters.PRETTY_MAIN_GSON;
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(dataFile));
        } catch (FileNotFoundException e) {
            CobblemonBingo.getLog().error("Something went wrong attempting to read the Player Data, new Player Perhaps?");
            return null;
        }
        return gson.fromJson(reader, Player.class);


    }

    public Player getPlayer(UUID uuid) {
        if (CobblemonBingo.wrapper.playerCache.containsKey(uuid))
            return CobblemonBingo.wrapper.playerCache.get(uuid);
        if (CobblemonBingo.dbConfig.enabled) {
            if (this.database != null) {
                return this.database.getPlayer(uuid);
            }
        }

        File dir = CobblemonBingo.getPlayerStorageDir();
        dir.mkdirs();


        File dataFile = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(uuid)));
        Gson gson = Adapters.PRETTY_MAIN_GSON;
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(dataFile));
        } catch (FileNotFoundException e) {
            CobblemonBingo.getLog().error("Something went wrong attempting to read the Player Data, new Player Perhaps?");
            return null;
        }
        return gson.fromJson(reader, Player.class);


    }

    public void savePlayer(Player player) {
        if (CobblemonBingo.dbConfig.enabled) {
            //save to db
            try {
                if (this.database != null)
                {
                    this.database.save(player);
                }
            } catch (NoClassDefFoundError e) {
                CobblemonBingo.getLog().error("Database support is not enabled, please check your configuration");
                CobblemonBingo.getLog().error("It is possible that the MongoDB driver is missing- please redownload the plugin you use to launch the driver");
                return;
            }

        } else {

            File dir = CobblemonBingo.getPlayerStorageDir();
            dir.mkdirs();

            File file = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(player.uuid)));
            Gson gson = Adapters.PRETTY_MAIN_GSON;
            JsonReader reader = null;
            try {
                reader = new JsonReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (reader == null) {
                CobblemonBingo.getLog().error("Something went wrong attempting to read the Player Data");
                return;
            }

            try {
                FileWriter writer = new FileWriter(file);
                writer.write(gson.toJson(player));
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        player.updateCache();
    }

    public void savePlayerNoCache(Player player) {
        if (CobblemonBingo.dbConfig.enabled) {
            try {
                if (this.database != null)
                {
                    this.database.save(player);
                }
            } catch (NoClassDefFoundError e) {
                CobblemonBingo.getLog().error("Database support is not enabled, please check your configuration");
                CobblemonBingo.getLog().error("It is possible that the MongoDB driver is missing- please redownload the plugin you use to launch the driver");
                return;
            }

        } else {

            File dir = CobblemonBingo.getPlayerStorageDir();
            dir.mkdirs();

            File file = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(player.uuid)));
            Gson gson = Adapters.PRETTY_MAIN_GSON;
            JsonReader reader = null;
            try {
                reader = new JsonReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (reader == null) {
                CobblemonBingo.getLog().error("Something went wrong attempting to read the Player Data");
                return;
            }

            try {
                FileWriter writer = new FileWriter(file);
                writer.write(gson.toJson(this));
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void makePlayer(ServerPlayer player) {

        Player dexPlayer = new Player(player.getUUID());
        if (CobblemonBingo.dbConfig.enabled)
        {
            if (this.database != null)
            {
                this.database.makePlayer(player.getUUID());
            }
        } else {
            File dir = CobblemonBingo.getPlayerStorageDir();
            dir.mkdirs();


            File file = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(player.getUUID())));
            if (file.exists()) {
                CobblemonBingo.getLog().error("There was an issue generating the Player, Player already exists? Ending function");
                return;
            }

            Gson gson = Adapters.PRETTY_MAIN_GSON;
            String json = gson.toJson(dexPlayer);

            try {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write(json);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean makePlayer(UUID uuid, boolean recreateIfExists) {
        boolean playerExists = false;
        Player bingoPlayer = new Player(uuid);
        if (CobblemonBingo.dbConfig.enabled) {
            if (this.database != null) {
                return this.database.makePlayer(uuid, recreateIfExists);
            }
        } else {
            File dir = CobblemonBingo.getPlayerStorageDir();
            dir.mkdirs();


            File file = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(uuid)));
            if (file.exists()) {
                CobblemonBingo.getLog().error("There was an issue generating the Player, Player already exists? Ending function");
                playerExists = true;
                if (!recreateIfExists)
                    return playerExists;
                Player bPlayer = new Player(uuid);
                //remove old file
                if (!file.delete()) {
                    CobblemonBingo.getLog().error("There was an issue deleting the old player file, cannot recreate player");
                    return playerExists;
                }
                bingoPlayer = bPlayer;
            }

            Gson gson = Adapters.PRETTY_MAIN_GSON;
            String json = gson.toJson(bingoPlayer);

            try {
                if (!file.exists())
                    if (file.createNewFile()) {
                        FileWriter writer = new FileWriter(file);
                        writer.write(json);
                        writer.close();
                        playerExists = true;
                    } else {
                        CobblemonBingo.getLog().error("There was an issue creating the player file, cannot create player");
                    }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return playerExists;
    }


    public List<Player> getAllPlayersFromFiles(boolean database)
    {

        List<UUID> addedPlayers = new ArrayList<>();

        List<Player> playerList = new ArrayList<>();


        File dir = CobblemonBingo.getPlayerStorageDir();
        if (dir != null) {
            for (File f : dir.listFiles()) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(f.getName().replace(".json", ""));
                } catch (IllegalArgumentException e)
                {
                    e.printStackTrace();
                    continue;
                }
                if (addedPlayers.contains(uuid))
                    continue;
                Player p;
                if (database)
                    p = getPlayer(uuid);
                else p = getPlayerFlatFile(uuid);
                if (p == null) {
                    CobblemonBingo.getLog().warn("Failed retrieving data for %uuid%".replace("%uuid%", uuid.toString()));
                    continue;
                }
                playerList.add(p);
                addedPlayers.add(uuid);
            }
        }

        return playerList;
    }
}
