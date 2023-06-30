package io.github.adainish.cobblemonbingoforge.subscriptions;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
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
        CobblemonEvents.PLAYER_JOIN.subscribe(Priority.NORMAL, serverPlayer -> {

            Player player = PlayerStorage.getPlayer(serverPlayer.getUUID());
            if (player == null) {
                PlayerStorage.makePlayer(serverPlayer.getUUID());
                player = PlayerStorage.getPlayer(serverPlayer.getUUID());

            }

            if (player != null) {
                player.setUsername(serverPlayer.getName().getString());
                player.updateCache();
            }

            return Unit.INSTANCE;
        });

        CobblemonEvents.PLAYER_QUIT.subscribe(Priority.NORMAL, serverPlayer -> {

            if (serverPlayer != null) {
                Player player = PlayerStorage.getPlayer(serverPlayer.getUUID());
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
            Player player = PlayerStorage.getPlayer(event.getPlayer().getUUID());
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
