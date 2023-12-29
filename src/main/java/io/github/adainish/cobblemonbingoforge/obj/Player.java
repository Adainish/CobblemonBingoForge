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
import com.google.gson.Gson;
import io.github.adainish.cobblemonbingoforge.CobblemonBingoForge;
import io.github.adainish.cobblemonbingoforge.util.Adapters;
import io.github.adainish.cobblemonbingoforge.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Player
{
    public UUID uuid;
    public String userName;
    public List<BingoCard> bingoCardList = new ArrayList<>();

    public Player(UUID uuid)
    {
        this.uuid = uuid;
    }

    public void sendMessage(String msg)
    {
        if (msg == null)
            return;
        if (msg.isEmpty())
            return;
        Util.send(uuid, msg);
    }

    public String getUsername()
    {
        if (this.userName != null)
            return userName;
        return "";
    }

    public Document toDocument() {
        Gson gson  = Adapters.PRETTY_MAIN_GSON;
        String json = gson.toJson(this);
        return Document.parse(json);
    }

    public void saveNoCache()
    {
        CobblemonBingoForge.playerStorage.savePlayerNoCache(this);
    }


    public void setUsername(String name)
    {
        this.userName = name;
    }

    public void save()
    {
        CobblemonBingoForge.playerStorage.savePlayer(this);
    }

    public void updateCache()
    {
        CobblemonBingoForge.instance.wrapper.playerCache.put(uuid, this);
    }

    public void updateBingo()
    {
        if (this.bingoCardList.isEmpty())
            this.bingoCardList.add(new BingoCard());
    }

    public void expireOldCards()
    {
        List<BingoCard> toRemove = new ArrayList<>();

        bingoCardList.forEach(bingoCard -> {
            if (bingoCard.expired())
                toRemove.add(bingoCard);
        });
        if (!toRemove.isEmpty())
            bingoCardList.removeAll(toRemove);
    }

    //gui code

    public GooeyButton filler() {
        return GooeyButton.builder()
                .display(new ItemStack(Items.GRAY_STAINED_GLASS_PANE))
                .build();
    }

    public List<Button> bingoButtons()
    {
        List<Button> buttons = new ArrayList<>();

        bingoCardList.forEach(bingoCard -> {
            GooeyButton button = GooeyButton.builder()
                    .title(Util.formattedString("&aBingo"))
                    .lore(Util.formattedArrayList(Arrays.asList("&7Click to view this bingo card")))
                    .onClick(b -> {
                        bingoCard.open(b.getPlayer());
                    })
                    .display(new ItemStack(Items.ENCHANTED_BOOK))
                    .build();
            buttons.add(button);
        });

        return buttons;
    }

    public LinkedPage bingoScrollPages()
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

        return PaginationHelper.createPagesFromPlaceholders(builder.build(), bingoButtons(), LinkedPage.builder().template(builder.build()));

    }

    public void open()
    {
        if (bingoCardList.isEmpty())
        {
            Util.send(uuid, "&cYou do not have any active bingo cards!");
        } else {
            if (bingoCardList.size() > 1)
            {
                UIManager.openUIForcefully(Util.getPlayer(uuid), bingoScrollPages());
            } else {
                UIManager.openUIForcefully(Util.getPlayer(uuid), bingoCardList.get(0).bingoMainPage());
            }
        }
    }



}
