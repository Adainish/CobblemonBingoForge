package io.github.adainish.cobblemonbingo.util;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.util.adapters.PokemonPropertiesAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Modifier;

public class Adapters
{
    public static Gson PRETTY_MAIN_GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .registerTypeAdapter(ItemStack.class, ItemStackAdapter.class)
            .registerTypeAdapter(PokemonProperties.class, new PokemonPropertiesAdapter(true))
            .excludeFieldsWithModifiers(Modifier.TRANSIENT)
            .create();
}
