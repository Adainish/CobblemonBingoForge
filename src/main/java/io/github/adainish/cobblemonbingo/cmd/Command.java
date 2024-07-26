package io.github.adainish.cobblemonbingo.cmd;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.adainish.cobblemonbingo.CobblemonBingo;
import io.github.adainish.cobblemonbingo.obj.Player;
import io.github.adainish.cobblemonbingo.util.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class Command
{
    public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("bingo")
                .executes(cc -> {
                    //open gui
                    if (cc.getSource().isPlayer()) {
                        Player player = CobblemonBingo.playerStorage.getPlayer(cc.getSource().getPlayer().getUUID());
                        if (player != null) {
                            try {
                                player.open();
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                                Util.send(cc.getSource(), "&cFailed to load your bingo data!");
                            }

                        }
                    } else {
                        Util.send(cc.getSource(), "&eDid you mean to use /bingo reload?");
                    }
                    return 1;
                })
                .then(Commands.literal("reload")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                        .executes(cc -> {
                            CobblemonBingo.instance.reload();
                            Util.send(cc.getSource(), "&eReloaded the bingo card plugin, please check the console for any errors.");
                            return 1;
                        })
                )
                ;

    }
}
