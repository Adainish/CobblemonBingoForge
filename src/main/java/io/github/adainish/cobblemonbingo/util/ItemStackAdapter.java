package io.github.adainish.cobblemonbingo.util;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.lang.reflect.Type;

/**
 * A type adapter for {@link ItemStack}
 * <p> This class is used to serialize and deserialize {@link ItemStack} objects.
 * </p>
 * {@code @Author} Adainish
 */
public class ItemStackAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {
    /**
     * Deserialize the Json data to an ItemStack
     *
     * @param json    The Json data being deserialized
     * @param typeOfT The type of the Object to deserialize to
     * @param context The context of the deserialization
     * @return The deserialized ItemStack
     * @throws JsonParseException If the Json data is invalid
     */
    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null || json.isJsonNull()) {
            return new ItemStack(Items.PAPER);
        }
        if (json.isJsonObject()) {
            return ItemStack.CODEC.parse(JsonOps.INSTANCE, json).result().orElse(new ItemStack(Items.PAPER));
        }
        throw new JsonParseException("Invalid ItemStack JSON: " + json);
    }


    /**
     * Serialize the ItemStack to Json
     *
     * @param src       the object that needs to be converted to Json.
     * @param typeOfSrc the actual type (fully genericized version) of the source object.
     * @param context   The context of the serialization
     * @return The serialized JsonElement
     */
    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
        if (src.isEmpty()) {
            return context.serialize("", String.class);
        }
        return ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, src).result().orElse(JsonNull.INSTANCE);
    }
}