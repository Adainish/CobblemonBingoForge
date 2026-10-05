package io.github.adainish.cobblemonbingo.storage;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.github.adainish.cobblemonbingo.CobblemonBingo;
import io.github.adainish.cobblemonbingo.obj.Player;
import io.github.adainish.cobblemonbingo.shard.ShardBridge;
import io.github.adainish.cobblemonbingo.util.Adapters;
import io.github.adainish.cobblemonbingo.util.Util;
import net.minecraft.server.level.ServerPlayer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerStorage
{
    public Database database;

    /** Sharded only: the database revision each cached player was loaded/last saved at. */
    private final Map<UUID, Long> revisions = new HashMap<>();

    public void saveAll()
    {
        CobblemonBingo.getLog().warn("Saving player data...");
        // only players online here: anyone else may be playing (and saving) on another instance
        new ArrayList<>(CobblemonBingo.wrapper.playerCache.values()).forEach(player -> {
            if (Util.getPlayer(player.uuid) != null)
                player.saveNoCache();
        });
        CobblemonBingo.wrapper.playerCache.clear();
        CobblemonBingo.wrapper.loading.clear();
        revisions.clear();
    }

    /** Sharding needs every instance on the same MongoDB database; local files would split player data. */
    public boolean sharedStorageReady() {
        return CobblemonBingo.dbConfig != null && CobblemonBingo.dbConfig.enabled && this.database != null && this.database.isConnected();
    }

    /**
     * Loads the player for a session on this server, bypassing the cache. When sharded this reads the shared
     * database together with its revision (so later saves can't overwrite newer data written by another instance).
     */
    public Player loadForSession(UUID uuid) {
        CobblemonBingo.wrapper.playerCache.remove(uuid);
        revisions.remove(uuid);
        if (ShardBridge.sharded() && sharedStorageReady()) {
            Database.Loaded loaded;
            try {
                loaded = this.database.loadWithRevision(uuid);
            } catch (Exception e) {
                throw new IllegalStateException("Could not read bingo data for " + uuid, e);
            }
            if (loaded == null || loaded.player() == null)
                return null;
            revisions.put(uuid, loaded.revision());
            return loaded.player();
        }
        return getPlayer(uuid);
    }

    /** Drops a player from every cache (on logout). */
    public void evict(UUID uuid) {
        CobblemonBingo.wrapper.playerCache.remove(uuid);
        CobblemonBingo.wrapper.loading.remove(uuid);
        revisions.remove(uuid);
    }

    /**
     * Sharded save: a conditional write so a stale copy can never overwrite newer data. Returns false when nothing
     * was written (database error, or another instance changed the data first; the newer data is then reloaded).
     */
    private boolean saveShared(Player player) {
        Player cached = CobblemonBingo.wrapper.playerCache.get(player.uuid);
        if (cached != null && cached != player) {
            CobblemonBingo.getLog().warn("Skipped saving an outdated copy of bingo data for " + player.uuid);
            return false;
        }
        Long expected = revisions.get(player.uuid);
        long result;
        try {
            result = this.database.saveIfRevision(player, expected == null ? -1 : expected);
        } catch (Exception e) {
            // keep the cached copy and its revision: the next save retries
            CobblemonBingo.getLog().error("Failed to save bingo data for " + player.uuid, e);
            return false;
        }
        if (result != Database.CONFLICT) {
            revisions.put(player.uuid, result);
            return true;
        }
        // another instance wrote newer data: keep theirs instead of rolling it back
        CobblemonBingo.getLog().warn("Bingo data for " + player.uuid + " was changed on another server; reloading the newer data");
        try {
            Database.Loaded fresh = this.database.loadWithRevision(player.uuid);
            if (fresh != null && fresh.player() != null) {
                revisions.put(player.uuid, fresh.revision());
                if (cached != null)
                    fresh.player().updateCache();
            }
        } catch (Exception e) {
            CobblemonBingo.getLog().error("Failed to reload bingo data for " + player.uuid, e);
        }
        return false;
    }

    /**
     * Saves right after a card slot was completed, before its rewards are handed out. When sharded the rewards may
     * only be granted when this returns true (the completion was stored); a single server always grants them.
     */
    public boolean saveClaim(Player player) {
        if (ShardBridge.sharded() && sharedStorageReady())
            return saveShared(player);
        savePlayer(player);
        return true;
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
        // still loading (sharded handoff): never hand out a copy that could overwrite the real data
        if (CobblemonBingo.wrapper.loading.contains(uuid))
            return null;
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
        if (ShardBridge.sharded() && sharedStorageReady()) {
            saveShared(player);
            return;
        }
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
        if (ShardBridge.sharded() && sharedStorageReady()) {
            saveShared(player);
            return;
        }
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
                writer.write(gson.toJson(player));
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
