package io.github.adainish.cobblemonbingo.util;

import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.adainish.cobblemonbingo.CobblemonBingo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

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

    public static List<Component> formattedComponentList(List<String> s) {
        List<Component> list = new ArrayList<>();
        for (String str : s)
            list.add(Component.literal(formattedString(str)));
        return list;
    }

    public static ServerPlayer getPlayer(UUID uuid) {
        return CobblemonBingo.getServer().getPlayerList().getPlayer(uuid);
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
            CobblemonBingo.getServer().getCommands().getDispatcher().execute(cmd, CobblemonBingo.getServer().createCommandSourceStack());
        } catch (CommandSyntaxException e) {
            CobblemonBingo.getLog().error(e);
        }
    }

    public static Holder<Enchantment> getEnchantment(ResourceKey<Enchantment> enchantmentResourceKey) {
        return CobblemonBingo.getServer().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantmentResourceKey);
    }
}
