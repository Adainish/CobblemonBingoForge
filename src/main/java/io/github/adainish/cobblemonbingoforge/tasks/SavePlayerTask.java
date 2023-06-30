package io.github.adainish.cobblemonbingoforge.tasks;

import io.github.adainish.cobblemonbingoforge.CobblemonBingoForge;
import io.github.adainish.cobblemonbingoforge.obj.Player;

import java.util.ArrayList;
import java.util.List;

public class SavePlayerTask implements Runnable
{
    @Override
    public void run() {
        List<Player> playerList = new ArrayList<>(CobblemonBingoForge.instance.dataWrapper.playerCache.values());
        playerList.forEach(player -> {
            player.expireOldCards();
            player.updateBingo();
            player.save();
        });
    }
}
