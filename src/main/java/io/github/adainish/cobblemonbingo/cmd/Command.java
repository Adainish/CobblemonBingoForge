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
                .then(Commands.literal("reset")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                        .executes(cc -> {
                            if (cc.getSource().isPlayer()) {
                                Player player = CobblemonBingo.playerStorage.getPlayer(cc.getSource().getPlayer().getUUID());
                                if (player != null) {
                                    player.bingoCardList.clear();
                                    Util.send(cc.getSource(), "&aYour bingo cards have been reset.");
                                } else {
                                    Util.send(cc.getSource(), "&cFailed to load your bingo data!");
                                }
                            } else {
                                Util.send(cc.getSource(), "&cOnly players can use this command.");
                            }
                            return 1;
                        })
                )
                .then(Commands.literal("givecard")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                        .executes(cc -> {
                            Util.send(cc.getSource(), "&eUsage: /bingo givecard <player>");
                            return 1;
                        })
                        .then(Commands.argument("player", net.minecraft.commands.arguments.EntityArgument.player())
                                .executes(cc -> {
                                    Player player = CobblemonBingo.playerStorage.getPlayer(net.minecraft.commands.arguments.EntityArgument.getPlayer(cc, "player").getUUID());
                                    if (player != null) {
                                        player.giveNewCard();
                                        Util.send(cc.getSource(), "&aGave " + player.getUsername() + " a new bingo card.");
                                        Util.send(player.uuid, "&aYou have been given a new bingo card by an administrator.");
                                    } else {
                                        Util.send(cc.getSource(), "&cFailed to load that player's bingo data!");
                                    }
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("resetplayer")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                        .executes(cc -> {
                            Util.send(cc.getSource(), "&eUsage: /bingo resetplayer <player>");
                            Util.send(cc.getSource(), "&4&lWARNING: &cThis will clear the player's bingo data with no confirmation!");
                            Util.send(cc.getSource(), "&cMake sure you have the correct username!");
                            //fully creates a new player entry for bingo in the data set if the player data doesn't exist or went corrupt
                            Util.send(cc.getSource(), "&4&lWARNING: &cFully creates a new player entry for bingo in the data set if the player data doesn't exist or went corrupt!");
                            Util.send(cc.getSource(), "&4&lWARNING: &cThis will clear the player's bingo data with no confirmation!");
                            return 1;
                        })
                        .then(Commands.argument("player", net.minecraft.commands.arguments.EntityArgument.player())
                                .executes(cc -> {
                                    Player player = CobblemonBingo.playerStorage.getPlayer(net.minecraft.commands.arguments.EntityArgument.getPlayer(cc, "player").getUUID());
                                    if (player != null) {
                                        player.bingoCardList.clear();
                                        Util.send(cc.getSource(), "&a" + player.getUsername() + "'s bingo cards have been reset.");
                                        Util.send(player.uuid, "&aYour bingo cards have been reset by an administrator.");
                                    } else {
                                        Util.send(cc.getSource(), "&cFailed to load that player's bingo data!");
                                        Util.send(cc.getSource(), "&cDetecting if corrupt data exists!");
                                        if (CobblemonBingo.playerStorage.makePlayer(net.minecraft.commands.arguments.EntityArgument.getPlayer(cc, "player").getUUID(), true)) {
                                            Util.send(cc.getSource(), "&aCorrupt or non-existent data detected, created new player data for them!");
                                        } else {
                                            Util.send(cc.getSource(), "&cFailed to create new player data for them, please check the console for errors.");
                                        }
                                    }
                                    return 1;
                                })
                        )
                )
                ;

    }
}
