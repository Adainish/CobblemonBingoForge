package io.github.adainish.cobblemonbingo.tasks;

import io.github.adainish.cobblemonbingo.CobblemonBingo;
import io.github.adainish.cobblemonbingo.obj.Player;
import io.github.adainish.cobblemonbingo.util.Util;

import java.util.ArrayList;
import java.util.List;

public class SavePlayerTask implements Runnable
{
    @Override
    public void run() {
        List<Player> playerList = new ArrayList<>(CobblemonBingo.wrapper.playerCache.values());
        playerList.forEach(player -> {
            // only players online here: an offline copy is stale (and may be newer on another instance)
            if (Util.getPlayer(player.uuid) == null) {
                CobblemonBingo.playerStorage.evict(player.uuid);
                return;
            }
            player.expireOldCards();
            player.updateBingo();
            player.save();
        });
    }
}
