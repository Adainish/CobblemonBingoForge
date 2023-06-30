package io.github.adainish.cobblemonbingoforge.storage;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.github.adainish.cobblemonbingoforge.CobblemonBingoForge;
import io.github.adainish.cobblemonbingoforge.obj.Player;
import io.github.adainish.cobblemonbingoforge.util.Adapters;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerStorage
{
    public static void makePlayer(UUID uuid) {
        File dir = CobblemonBingoForge.getPlayerStorageDir();
        dir.mkdirs();


        Player playerData = new Player(uuid);

        File file = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(uuid)));
        if (file.exists()) {
            return;
        }

        Gson gson = Adapters.PRETTY_MAIN_GSON;
        String json = gson.toJson(playerData);

        try {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void makePlayer(ServerPlayer player) {
        File dir = CobblemonBingoForge.getPlayerStorageDir();
        dir.mkdirs();


        Player playerData = new Player(player.getUUID());

        File file = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(player.getUUID())));
        if (file.exists()) {
            return;
        }

        Gson gson = Adapters.PRETTY_MAIN_GSON;
        String json = gson.toJson(playerData);

        try {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void savePlayer(Player player) {

        File dir = CobblemonBingoForge.getPlayerStorageDir();
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
            CobblemonBingoForge.getLog().error("Something went wrong attempting to read the Player Data");
            return;
        }


        try {
            FileWriter writer = new FileWriter(file);
            writer.write(gson.toJson(player));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.updateCache();
    }

    public static List<UUID> getAllPlayerUUIDS()
    {
        List<UUID> uuids = new ArrayList<>();

        File dir = CobblemonBingoForge.getPlayerStorageDir();
        if (dir != null) {
            for (File f : dir.listFiles()) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(f.getName().replace(".json", ""));
                } catch (IllegalArgumentException e)
                {
                    continue;
                }

                uuids.add(uuid);

            }
        }

        return uuids;
    }

    public static List<Player> getAllPlayers()
    {

        List<UUID> addedPlayers = new ArrayList<>();

        List<Player> playerList = new ArrayList<>();

        for (Player p:CobblemonBingoForge.instance.dataWrapper.playerCache.values()) {
            playerList.add(p);
            addedPlayers.add(p.uuid);
        }

        File dir = CobblemonBingoForge.getPlayerStorageDir();
        if (dir != null) {
            for (File f : dir.listFiles()) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(f.getName().replace(".json", ""));
                } catch (IllegalArgumentException e)
                {
                    continue;
                }
                if (addedPlayers.contains(uuid))
                    continue;
                Player p = getPlayer(uuid);
                if (p == null)
                    continue;
                playerList.add(p);
                addedPlayers.add(uuid);
            }
        }

        return playerList;
    }

    @Nullable
    public static Player getPlayer(UUID uuid) {
        File dir = CobblemonBingoForge.getPlayerStorageDir();
        dir.mkdirs();

        if (CobblemonBingoForge.instance.dataWrapper.playerCache.containsKey(uuid))
            return CobblemonBingoForge.instance.dataWrapper.playerCache.get(uuid);

        File guildFile = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(uuid)));
        Gson gson = Adapters.PRETTY_MAIN_GSON;
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(guildFile));
        } catch (FileNotFoundException e) {
            CobblemonBingoForge.getLog().error("Detected non-existing player, making new player data file");
            return null;
        }

        return gson.fromJson(reader, Player.class);
    }
}
