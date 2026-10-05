package io.github.adainish.cobblemonbingo.gui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import io.github.adainish.cobblemonbingo.CobblemonBingo;
import io.github.adainish.cobblemonbingo.manager.BingoManager;
import io.github.adainish.cobblemonbingo.obj.Reward;
import io.github.adainish.cobblemonbingo.util.Util;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Read-only overview of the rewards a player can receive from their bingo card.
 */
public class RewardsGUI
{
    public enum RewardCategory
    {
        SLOT("&a&lPer Pokémon", Items.GOLD_NUGGET, BingoManager::getPossibleSlotRewards,
                "&7Rolled every time you capture", "&7a Pokémon on your card."),
        ROW("&e&lLine", Items.GOLD_INGOT, BingoManager::getPossibleRowRewards,
                "&7Rolled when you complete a", "&7full row or column."),
        COLUMN("&6&lColumn", Items.IRON_INGOT, BingoManager::getPossibleColumnRewards,
                "&7Rolled when you complete", "&7a full column."),
        DIAGONAL("&d&lDiagonal", Items.AMETHYST_SHARD, BingoManager::getPossibleDiagonalRewards,
                "&7Rolled when you complete", "&7a full diagonal."),
        COMPLETION("&b&lFull Card", Items.NETHER_STAR, BingoManager::getPossibleCompletionRewards,
                "&7Rolled when you complete", "&7every Pokémon on your card.");

        public final String title;
        public final Item icon;
        private final Function<BingoManager, List<String>> rewardIds;
        public final List<String> description;

        RewardCategory(String title, Item icon, Function<BingoManager, List<String>> rewardIds, String... description)
        {
            this.title = title;
            this.icon = icon;
            this.rewardIds = rewardIds;
            this.description = List.of(description);
        }

        public List<String> getRewardIds()
        {
            List<String> ids = rewardIds.apply(CobblemonBingo.config.bingoManager);
            return ids == null ? new ArrayList<>() : ids;
        }
    }

    /**
     * Called when the player presses the back button on the category menu, null shows a close button instead.
     */
    private final Consumer<ServerPlayer> onBack;

    public RewardsGUI(Consumer<ServerPlayer> onBack)
    {
        this.onBack = onBack;
    }

    public void open(ServerPlayer serverPlayer)
    {
        UIManager.openUIForcefully(serverPlayer, categoryPage());
    }

    public GooeyButton filler() {
        return GooeyButton.builder()
                .display(new ItemStack(Items.GRAY_STAINED_GLASS_PANE))
                .with(DataComponents.CUSTOM_NAME, Component.literal(" "))
                .build();
    }

    private GooeyButton backButton(Consumer<ServerPlayer> action)
    {
        if (action == null) {
            return GooeyButton.builder()
                    .display(new ItemStack(Items.BARRIER))
                    .with(DataComponents.CUSTOM_NAME, Component.literal(Util.formattedString("&cClose")))
                    .onClick(b -> UIManager.closeUI(b.getPlayer()))
                    .build();
        }
        return GooeyButton.builder()
                .display(new ItemStack(Items.ARROW))
                .with(DataComponents.CUSTOM_NAME, Component.literal(Util.formattedString("&eBack")))
                .onClick(b -> action.accept(b.getPlayer()))
                .build();
    }

    private String title()
    {
        return Util.formattedString(CobblemonBingo.config.bingoManager.rewardsGUITitle);
    }

    public GooeyPage categoryPage()
    {
        ChestTemplate.Builder builder = ChestTemplate.builder(3);
        builder.fill(filler());

        int column = 2;
        for (RewardCategory category : RewardCategory.values()) {
            List<String> lore = new ArrayList<>(category.description);
            lore.add("");
            lore.add("&7Possible rewards: &f" + resolveRewards(category).size());
            lore.add("&eClick to view!");
            GooeyButton button = GooeyButton.builder()
                    .display(new ItemStack(category.icon))
                    .with(DataComponents.CUSTOM_NAME, Component.literal(Util.formattedString(category.title)))
                    .with(DataComponents.LORE, new ItemLore(Util.formattedComponentList(lore)))
                    .onClick(b -> UIManager.openUIForcefully(b.getPlayer(), rewardListPage(category)))
                    .build();
            builder.set(1, column++, button);
        }

        builder.set(2, 4, backButton(onBack));

        return GooeyPage.builder()
                .title(title())
                .template(builder.build())
                .build();
    }

    /**
     * Resolves the configured reward ids of a category into rewards, paired with their drop weight.
     * Mirrors the weighting used by {@link BingoManager} when picking a reward.
     */
    private LinkedHashMap<Reward, Integer> resolveRewards(RewardCategory category)
    {
        LinkedHashMap<Reward, Integer> weights = new LinkedHashMap<>();
        for (String id : category.getRewardIds()) {
            Reward reward = CobblemonBingo.rewardsConfig.rewardManager.rewards.get(id);
            if (reward == null)
                continue;
            int weight = Math.max(0, (int) Math.ceil(reward.chance));
            weights.merge(reward, weight, Integer::sum);
        }
        return weights;
    }

    public List<Button> rewardButtons(RewardCategory category)
    {
        LinkedHashMap<Reward, Integer> weights = resolveRewards(category);
        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();

        List<Map.Entry<Reward, Integer>> entries = new ArrayList<>(weights.entrySet());
        entries.sort(Comparator.comparingInt(e -> e.getKey().guiDisplayOrder));

        List<Button> buttons = new ArrayList<>();
        for (Map.Entry<Reward, Integer> entry : entries) {
            Reward reward = entry.getKey();
            List<String> lore = new ArrayList<>();
            if (reward.lore != null)
                lore.addAll(reward.lore);
            lore.add("");
            double percentage = totalWeight == 0 ? 0 : (entry.getValue() * 100.0) / totalWeight;
            lore.add("&7Chance: &e" + String.format(Locale.ROOT, "%.1f", percentage) + "%");

            ItemStack display = reward.icon == null || reward.icon.isEmpty() ? new ItemStack(Items.CHEST) : reward.icon.copy();
            GooeyButton button = GooeyButton.builder()
                    .display(display)
                    .with(DataComponents.CUSTOM_NAME, Component.literal(Util.formattedString(reward.prettyTitle == null ? reward.identifier : reward.prettyTitle)))
                    .with(DataComponents.LORE, new ItemLore(Util.formattedComponentList(lore)))
                    .build();
            buttons.add(button);
        }

        if (buttons.isEmpty()) {
            buttons.add(GooeyButton.builder()
                    .display(new ItemStack(Items.BARRIER))
                    .with(DataComponents.CUSTOM_NAME, Component.literal(Util.formattedString("&cNo rewards")))
                    .with(DataComponents.LORE, new ItemLore(Util.formattedComponentList(List.of("&7There are no rewards set up for this category."))))
                    .build());
        }
        return buttons;
    }

    public LinkedPage rewardListPage(RewardCategory category)
    {
        ChestTemplate.Builder builder = ChestTemplate.builder(6);
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

        builder.set(5, 3, previous)
                .set(5, 4, backButton(p -> UIManager.openUIForcefully(p, categoryPage())))
                .set(5, 5, next)
                .rectangle(1, 1, 4, 7, placeHolderButton);

        String pageTitle = title() + Util.formattedString(" &8- ") + Util.formattedString(category.title);
        return PaginationHelper.createPagesFromPlaceholders(builder.build(), rewardButtons(category), LinkedPage.builder().title(pageTitle).template(builder.build()));
    }
}
