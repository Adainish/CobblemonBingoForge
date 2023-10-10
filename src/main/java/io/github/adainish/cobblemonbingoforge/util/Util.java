package io.github.adainish.cobblemonbingoforge.util;

import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.adainish.cobblemonbingoforge.CobblemonBingoForge;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Util
{
    public static String formattedString(String s) {
        return s.replaceAll("&", "§");
    }

    public static List<String> formattedArrayList(List<String> list) {

        List<String> formattedList = new ArrayList<>();
        for (String s : list) {
            formattedList.add(formattedString(s));
        }

        return formattedList;
    }

    public static ServerPlayer getPlayer(UUID uuid) {
        return CobblemonBingoForge.getServer().getPlayerList().getPlayer(uuid);
    }

    public static void send(UUID uuid, String message) {
        if (message == null)
            return;
        if (message.isEmpty())
            return;
        ServerPlayer player = getPlayer(uuid);
        if (player != null)
            player.sendSystemMessage(Component.literal(((TextUtil.getMessagePrefix()).getString() + message).replaceAll("&([0-9a-fk-or])", "\u00a7$1")));
    }

    public static ItemStack returnIcon(Pokemon pokemon) {
        return PokemonItem.from(pokemon, 1);
    }


    public static void send(CommandSourceStack sender, String message) {
        sender.sendSystemMessage(Component.literal(((TextUtil.getMessagePrefix()).getString() + message).replaceAll("&([0-9a-fk-or])", "\u00a7$1")));
    }

    public static void runCommand(String cmd)
    {
        try {
            CobblemonBingoForge.getServer().getCommands().getDispatcher().execute(cmd, CobblemonBingoForge.getServer().createCommandSourceStack());
        } catch (CommandSyntaxException e) {
            CobblemonBingoForge.getLog().error(e);
        }
    }
}
