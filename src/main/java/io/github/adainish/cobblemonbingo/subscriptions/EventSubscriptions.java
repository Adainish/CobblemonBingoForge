package io.github.adainish.cobblemonbingo.subscriptions;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.platform.events.PlatformEvents;
import io.github.adainish.cobblemonbingo.CobblemonBingo;
import io.github.adainish.cobblemonbingo.obj.Player;
import io.github.adainish.cobblemonbingo.shard.ShardBridge;
import io.github.adainish.cobblemonbingo.util.Util;
import kotlin.Unit;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class EventSubscriptions
{

    public EventSubscriptions()
    {
        init();
    }

    public void loadPlayerSubscriptions()
    {
        PlatformEvents.SERVER_PLAYER_LOGIN.subscribe(Priority.NORMAL, serverPlayer -> {
            ServerPlayer player = serverPlayer.getPlayer();
            CobblemonBingo.instance.checkShardedStorage();
            // sharded: the previous instance may still be saving this player; load once it released them
            CobblemonBingo.wrapper.loading.add(player.getUUID());
            ShardBridge.whenReady(player.getUUID(), () -> loadPlayer(player));
            return Unit.INSTANCE;
        });

        PlatformEvents.SERVER_PLAYER_LOGOUT.subscribe(Priority.NORMAL, serverPlayer -> {

            if (serverPlayer != null) {
                UUID uuid = serverPlayer.getPlayer().getUUID();
                // only data loaded on this server is saved; a player still loading has nothing to save
                Player player = CobblemonBingo.wrapper.playerCache.get(uuid);
                if (player != null && !CobblemonBingo.wrapper.loading.contains(uuid)) {
                    player.save();
                }
                CobblemonBingo.playerStorage.evict(uuid);
            }
            return Unit.INSTANCE;
        });
    }

    private void loadPlayer(ServerPlayer serverPlayer)
    {
        UUID uuid = serverPlayer.getUUID();
        if (!CobblemonBingo.wrapper.loading.contains(uuid) || serverPlayer.hasDisconnected())
            return; // left before their data could be loaded
        Player player;
        try {
            CobblemonBingo.wrapper.loading.remove(uuid);
            player = CobblemonBingo.playerStorage.loadForSession(uuid);
            if (player == null) {
                CobblemonBingo.playerStorage.makePlayer(serverPlayer);
                player = CobblemonBingo.playerStorage.loadForSession(uuid);
            }
        } catch (Exception e) {
            // keep them "loading" so nothing creates fresh data over the stored copy
            CobblemonBingo.wrapper.loading.add(uuid);
            CobblemonBingo.getLog().error("Failed to load bingo data for " + serverPlayer.getName().getString(), e);
            Util.send(uuid, "&cYour bingo data could not be loaded, please rejoin or contact staff.");
            return;
        }

        if (player != null) {
            player.setUsername(serverPlayer.getName().getString());
            player.updateCache();
        }
    }

    public void init()
    {
        loadPlayerSubscriptions();
        CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.NORMAL, event -> {
            Player player = CobblemonBingo.playerStorage.getPlayer(event.getPlayer().getUUID());
            if (player != null)
            {
                if (!player.bingoCardList.isEmpty())
                {
                    player.bingoCardList.forEach(bingoCard -> {
                        bingoCard.update(player, event.getPokemon());
                    });
                }
            }
            return Unit.INSTANCE;
        });
    }
}
