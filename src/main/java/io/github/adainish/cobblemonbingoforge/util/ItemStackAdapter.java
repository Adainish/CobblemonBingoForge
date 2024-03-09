package io.github.adainish.cobblemonbingoforge.util;

import com.google.gson.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.TagParser;

import java.lang.reflect.Type;

public class ItemStackAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack>
{
    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        try
        {
            if (json == null)
                return new ItemStack(Items.PAPER);
            CompoundTag compoundTag = TagParser.parseTag(json.getAsString());
            //Convert json to NBT string, then to CompoundNBT
            ItemStack itemStack = ItemStack.of(compoundTag);
            return itemStack.isEmpty() ? new ItemStack(Items.PAPER) : itemStack;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context)
    {
        if (src.isEmpty())
            return context.serialize("", String.class);
        else
            return context.serialize(src.copy().serializeNBT().toString(), String.class);
    }
}