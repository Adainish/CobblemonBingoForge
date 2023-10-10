package io.github.adainish.cobblemonbingoforge.util;

import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Type;

public class ItemStackAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack>
{
    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        try
        {
            String nbtString = json.getAsString();
            if (nbtString == null || nbtString.isEmpty()) {
                return null;
            }

            ItemStack item = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(json.getAsString().toLowerCase())));
            return item.isEmpty() ? ItemStack.EMPTY : item;
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