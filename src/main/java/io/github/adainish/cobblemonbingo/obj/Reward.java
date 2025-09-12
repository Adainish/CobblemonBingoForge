package io.github.adainish.cobblemonbingo.obj;

import io.github.adainish.cobblemonbingo.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Reward
{
    public String identifier;
    public ItemStack icon;
    public String prettyTitle;
    public List<String> lore;
    public List<String> commands;
    public List<String> messages;
    public int guiDisplayOrder;
    public double chance;

    public Reward(String id)
    {
        this.identifier = id;
        this.icon = new ItemStack(Items.DIRT);
        this.prettyTitle = "&b&lExample Rewards";
        this.lore = new ArrayList<>(Arrays.asList("&eExample of a rewards"));
        this.commands = new ArrayList<>(Arrays.asList("broadcast test"));
        this.messages = new ArrayList<>(Arrays.asList("&eYou've received an example rewards"));
        this.guiDisplayOrder = 0;
        this.chance = 100;
    }

    public void execute(Player player)
    {
        this.commands.forEach(s -> {
            Util.runCommand(s.replace("%pl%", player.getUsername()));
        });
        this.messages.forEach(player::sendMessage);
    }
}
