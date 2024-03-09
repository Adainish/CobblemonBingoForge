package io.github.adainish.cobblemonbingoforge.util;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.util.adapters.NbtCompoundAdapter;
import com.cobblemon.mod.common.util.adapters.PokemonPropertiesAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Modifier;

public class Adapters
{
    public static Gson PRETTY_MAIN_GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .registerTypeAdapter(ItemStack.class, new ItemStackAdapter())
            .registerTypeAdapter(CompoundTag.class, NbtCompoundAdapter.INSTANCE)
            .registerTypeAdapter(PokemonProperties.class, new PokemonPropertiesAdapter(true))
            .excludeFieldsWithModifiers(Modifier.TRANSIENT)
            .create();
}
