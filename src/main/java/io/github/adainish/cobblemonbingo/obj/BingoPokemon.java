package io.github.adainish.cobblemonbingo.obj;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Species;
import net.minecraft.resources.ResourceLocation;

public class BingoPokemon
{
    public String resourceKey;
    public boolean completed = false;
    public int dexNumber = 0;
    public BingoPokemon(String resourceKey)
    {
        this.setResourceKey(resourceKey);
        setDexNumber();
    }

    public void setDexNumber()
    {
        try {
            this.dexNumber = PokemonSpecies.INSTANCE.getByIdentifier(new ResourceLocation(resourceKey)).create(1).getSpecies().getNationalPokedexNumber();
        } catch (NullPointerException e)
        {
            e.printStackTrace();
            this.dexNumber = 1;
        }
    }

    public Species getSpecies()
    {
        ResourceLocation resourceLocation = new ResourceLocation(resourceKey);
        return PokemonSpecies.INSTANCE.getByIdentifier(resourceLocation);
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public int getDexNumber()
    {
        return this.dexNumber;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
