package io.github.adainish.cobblemonbingo.obj;

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
import io.github.adainish.cobblemonbingo.CobblemonBingo;
import io.github.adainish.cobblemonbingo.util.Util;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.*;
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
        while (selectedBingoPokemon.size() < CobblemonBingo.config.bingoManager.bingoSize)
        {
            Species species = PokemonSpecies.random();
            if (CobblemonBingo.config.bingoManager.blacklistedSpeciesResourceKeys.contains(species.resourceIdentifier.toString()))
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
        return System.currentTimeMillis() >= (startedAt + TimeUnit.MINUTES.toMillis(CobblemonBingo.config.bingoManager.bingoTimerMinutes));
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

    public boolean hasCompletedRow(int row)
    {
        if (row < 1 || row > 4)
            return false;
        AtomicBoolean toReturn = new AtomicBoolean(true);
        List<BingoPokemon> bingoPokemons = sortedBingoPokemon();
        for (int i = (row - 1) * 7; i < row * 7; i++)
        {
            if (i >= bingoPokemons.size())
                break;
            if (!bingoPokemons.get(i).completed)
                toReturn.set(false);
        }
        return toReturn.get();
    }

    public boolean hasCompletedColumn(int column) {
        if (column < 1 || column > 7)
            return false;
        List<BingoPokemon> bingoPokemons = sortedBingoPokemon();
        AtomicBoolean toReturn = new AtomicBoolean(true);
        for (int i = column - 1; i < bingoPokemons.size(); i += 7) {
            if (!bingoPokemons.get(i).completed)
                toReturn.set(false);
        }
        return toReturn.get();
    }

    public boolean hasCompletedDiagonal1() {
        List<BingoPokemon> bingoPokemons = sortedBingoPokemon();
        int size = bingoPokemons.size();
        int min = Math.min(4, size / 7); // Number of rows
        for (int i = 0; i < min; i++) {
            int idx = i * 7 + i;
            if (idx >= size || !bingoPokemons.get(idx).completed)
                return false;
        }
        return true;
    }

    public boolean hasCompletedDiagonal2() {
        List<BingoPokemon> bingoPokemons = sortedBingoPokemon();
        int size = bingoPokemons.size();
        int min = Math.min(4, size / 7); // Number of rows
        for (int i = 0; i < min; i++) {
            int idx = i * 7 + (6 - i);
            if (idx >= size || !bingoPokemons.get(idx).completed)
                return false;
        }
        return true;
    }

    public void update(Player player, Pokemon pokemon)
    {
        if (selectedBingoPokemon.containsKey(pokemon.getSpecies().getResourceIdentifier().toString()))
        {
            if (!selectedBingoPokemon.get(pokemon.getSpecies().getResourceIdentifier().toString()).completed) {
                selectedBingoPokemon.get(pokemon.getSpecies().getResourceIdentifier().toString()).completed = true;
                try {
                    Reward slotReward = CobblemonBingo.config.bingoManager.getRandomSlotReward();
                    slotReward.execute(player);
                    if (hasCompletedRow(1) || hasCompletedRow(2) || hasCompletedRow(3) || hasCompletedRow(4) || hasCompletedColumn(1) || hasCompletedColumn(2) || hasCompletedColumn(3) || hasCompletedColumn(4) || hasCompletedColumn(5) || hasCompletedColumn(6) || hasCompletedColumn(7)) {
                        Reward lineReward = CobblemonBingo.config.bingoManager.getRandomRowReward();
                        lineReward.execute(player);
                    }
                    // Check for completed columns and give column reward
                    for (int col = 1; col <= 7; col++) {
                        if (hasCompletedColumn(col)) {
                            Reward columnReward = CobblemonBingo.config.bingoManager.getRandomColumnReward();
                            columnReward.execute(player);
                            break; // Only one column reward per update
                        }
                    }
                    if (hasCompletedDiagonal1() || hasCompletedDiagonal2()) {
                        Reward diagonalReward = CobblemonBingo.config.bingoManager.getRandomDiagonalReward();
                        diagonalReward.execute(player);
                    }
                    if (hasCompletedCard()) {
                        Reward completionReward = CobblemonBingo.config.bingoManager.getRandomCompletionReward();
                        completionReward.execute(player);
                    }
                } catch (Exception e) {
                    CobblemonBingo.getLog().error("Seems like you failed to set up rewards for Cobblemon Bingo properly, please check your config(s) for any errors!");
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
            String pokemonName = pokemon.getSpecies().showdownId();
            // capitalize first letter
            pokemonName = pokemonName.substring(0, 1).toUpperCase() + pokemonName.substring(1).toLowerCase();
            ItemStack display = Util.returnIcon(pokemon);
            if (bingoPokemon.completed) {
                display.enchant(Util.getEnchantment(Enchantments.EFFICIENCY), 1);
                display.set(DataComponents.ENCHANTMENTS, display.getEnchantments().withTooltip(false));

                lore.add("&a&lCompleted!");
            }
            else {
                lore.add("&c&l(&4&l!&c&l) &eYou need to capture a {pokemon} for it to count towards your bingo!".replace("{pokemon}", pokemonName));
            }
            GooeyButton button = GooeyButton.builder()
                    .display(display)
                    .with(DataComponents.CUSTOM_NAME, Component.literal((Util.formattedString("&b" + pokemonName))))
                    .onClick(b -> {
                        //info about the Pokémon like spawn data?
                    })
                    .with(DataComponents.LORE, new ItemLore((Util.formattedComponentList(lore))))
                    .build();
            buttons.add(button);
        });
        return buttons;
    }

    public LinkedPage bingoMainPage(String userName)
    {
        ChestTemplate.Builder builder = ChestTemplate.builder(5);
        builder.fill(filler());

        PlaceholderButton placeHolderButton = new PlaceholderButton();
        LinkedPageButton previous = LinkedPageButton.builder()
                .display(new ItemStack(Items.SPECTRAL_ARROW))
                .with(DataComponents.CUSTOM_NAME, Component.literal(Util.formattedString("Previous Page")))
                .linkType(LinkType.Previous)
                .build();

        LinkedPageButton next = LinkedPageButton.builder()
                .display(new ItemStack(Items.SPECTRAL_ARROW))
                .with(DataComponents.CUSTOM_NAME, Component.literal(Util.formattedString("Next Page")))
                .linkType(LinkType.Next)
                .build();

        builder.set(0, 3, previous)
                .set(0, 5, next)
                .rectangle(1, 1, 3, 7, placeHolderButton);

        return PaginationHelper.createPagesFromPlaceholders(builder.build(), bingoPokemonButtons(), LinkedPage.builder().title(Util.formattedString(CobblemonBingo.config.bingoManager.bingoGUITitle.replace("{player}", userName))).template(builder.build()));

    }

    public void open(ServerPlayer serverPlayer)
    {
        UIManager.openUIForcefully(serverPlayer, bingoMainPage(serverPlayer.getName().getString()));
    }
}
