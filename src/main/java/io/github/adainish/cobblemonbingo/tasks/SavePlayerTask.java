package io.github.adainish.cobblemonbingo.tasks;

import io.github.adainish.cobblemonbingo.CobblemonBingo;
import io.github.adainish.cobblemonbingo.obj.Player;

import java.util.ArrayList;
import java.util.List;

public class SavePlayerTask implements Runnable
{
    @Override
    public void run() {
        List<Player> playerList = new ArrayList<>(CobblemonBingo.wrapper.playerCache.values());
        playerList.forEach(player -> {
            player.expireOldCards();
            player.updateBingo();
            player.save();
        });
    }
}
