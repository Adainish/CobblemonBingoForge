package io.github.adainish.cobblemonbingoforge.obj;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import io.github.adainish.cobblemonbingoforge.CobblemonBingoForge;
import io.github.adainish.cobblemonbingoforge.util.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BingoCard
{
    public long startedAt;
    public String freeSlot = null;
    public HashMap<String, BingoPokemon> selectedBingoPokemon = new HashMap<>();


    public BingoCard()
    {
        generate();
    }

    public void generate()
    {
        while (selectedBingoPokemon.size() < CobblemonBingoForge.config.bingoManager.bingoSize)
        {
            Species species = PokemonSpecies.INSTANCE.random();
            if (CobblemonBingoForge.config.bingoManager.blacklistedSpeciesResourceKeys.contains(species.resourceIdentifier.toString()))
                continue;
            BingoPokemon bingoPokemon = new BingoPokemon(species.resourceIdentifier.toString());
            selectedBingoPokemon.put(species.resourceIdentifier.toString(), bingoPokemon);
        }
        this.startedAt = System.currentTimeMillis();
    }

    public boolean usedFreeSlot()
    {
        return freeSlot != null && selectedBingoPokemon.containsKey(freeSlot);
    }

    public boolean expired()
    {
        return System.currentTimeMillis() >= (startedAt + TimeUnit.MINUTES.toMillis(CobblemonBingoForge.config.bingoManager.bingoTimerMinutes));
    }

    public boolean hasCompletedCard()
    {
        AtomicBoolean toReturn = new AtomicBoolean(true);
        selectedBingoPokemon.values().forEach(bingoPokemon -> {
            if (!bingoPokemon.completed)
                toReturn.set(false);
        });
        return toReturn.get();
    }

    public void update(Player player, Pokemon pokemon)
    {
        if (selectedBingoPokemon.containsKey(pokemon.getSpecies().getResourceIdentifier().toString()))
        {
            if (!selectedBingoPokemon.get(pokemon.getSpecies().getResourceIdentifier().toString()).completed) {
                selectedBingoPokemon.get(pokemon.getSpecies().getResourceIdentifier().toString()).completed = true;
                try {
                    Reward slotReward = CobblemonBingoForge.config.bingoManager.getRandomSlotReward();
                    slotReward.execute(player);
                    if (hasCompletedCard()) {
                        Reward completionReward = CobblemonBingoForge.config.bingoManager.getRandomCompletionReward();
                        completionReward.execute(player);
                    }
                } catch (Exception e) {
                    CobblemonBingoForge.getLog().error("Seems like you failed to set up rewards for Cobblemon Bingo properly, please check your config(s) for any errors!");
                }

            }
        }
    }

    public GooeyButton filler() {
        return GooeyButton.builder()
                .display(new ItemStack(Items.GRAY_STAINED_GLASS_PANE))
                .build();
    }

    public List<BingoPokemon> sortedBingoPokemon()
    {
        List<BingoPokemon> bingoPokemons = new ArrayList<>(selectedBingoPokemon.values());
        bingoPokemons.sort(Comparator.comparing(BingoPokemon::getDexNumber));
        return bingoPokemons;
    }

    public List<Button> bingoPokemonButtons()
    {
        List<Button> buttons = new ArrayList<>();
        sortedBingoPokemon().forEach(bingoPokemon -> {
            Pokemon pokemon = bingoPokemon.getSpecies().create(1);
            List<String> lore = new ArrayList<>();
            if (bingoPokemon.completed)
                lore.add("&a&lCompleted!");
            else lore.add("&c&l(&4&l!&c&l) &eYou need to capture this pokemon for it to count towards your bingo!");
            GooeyButton button = GooeyButton.builder()
                    .title(Util.formattedString("&b" + pokemon.getSpecies().showdownId()))
                    .display(Util.returnIcon(bingoPokemon.getSpecies().create(1)))
                    .onClick(b -> {
                        //info?
                    })
                    .lore(Util.formattedArrayList(lore))
                    .build();
            buttons.add(button);
        });
        return buttons;
    }

    public LinkedPage bingoMainPage()
    {
        ChestTemplate.Builder builder = ChestTemplate.builder(5);
        builder.fill(filler());

        PlaceholderButton placeHolderButton = new PlaceholderButton();
        LinkedPageButton previous = LinkedPageButton.builder()
                .display(new ItemStack(Items.SPECTRAL_ARROW))
                .title(Util.formattedString("Previous Page"))
                .linkType(LinkType.Previous)
                .build();

        LinkedPageButton next = LinkedPageButton.builder()
                .display(new ItemStack(Items.SPECTRAL_ARROW))
                .title(Util.formattedString("Next Page"))
                .linkType(LinkType.Next)
                .build();

        builder.set(0, 3, previous)
                .set(0, 5, next)
                .rectangle(1, 1, 3, 7, placeHolderButton);

        return PaginationHelper.createPagesFromPlaceholders(builder.build(), bingoPokemonButtons(), LinkedPage.builder().template(builder.build()));

    }

    public void open(ServerPlayer serverPlayer)
    {
        UIManager.openUIForcefully(serverPlayer, bingoMainPage());
    }
}
