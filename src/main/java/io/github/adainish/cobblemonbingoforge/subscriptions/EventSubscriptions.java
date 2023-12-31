package io.github.adainish.cobblemonbingoforge.subscriptions;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.platform.events.PlatformEvents;
import io.github.adainish.cobblemonbingoforge.CobblemonBingoForge;
import io.github.adainish.cobblemonbingoforge.obj.Player;
import io.github.adainish.cobblemonbingoforge.storage.PlayerStorage;
import kotlin.Unit;

public class EventSubscriptions
{

    public EventSubscriptions()
    {
        init();
    }

    public void loadPlayerSubscriptions()
    {
        PlatformEvents.SERVER_PLAYER_LOGIN.subscribe(Priority.NORMAL, serverPlayer -> {

            Player player = CobblemonBingoForge.playerStorage.getPlayer(serverPlayer.getPlayer().getUUID());
            if (player == null) {
                CobblemonBingoForge.playerStorage.makePlayer(serverPlayer.getPlayer());
                player = CobblemonBingoForge.playerStorage.getPlayer(serverPlayer.getPlayer().getUUID());

            }

            if (player != null) {
                player.setUsername(serverPlayer.getPlayer().getName().getString());
                player.updateCache();
            }

            return Unit.INSTANCE;
        });

        PlatformEvents.SERVER_PLAYER_LOGOUT.subscribe(Priority.NORMAL, serverPlayer -> {

            if (serverPlayer != null) {
                Player player = CobblemonBingoForge.playerStorage.getPlayer(serverPlayer.getPlayer().getUUID());
                if (player != null) {
                    player.save();
                }
            }
            return Unit.INSTANCE;
        });
    }

    public void init()
    {
        loadPlayerSubscriptions();
        CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.NORMAL, event -> {
            Player player = CobblemonBingoForge.playerStorage.getPlayer(event.getPlayer().getUUID());
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
