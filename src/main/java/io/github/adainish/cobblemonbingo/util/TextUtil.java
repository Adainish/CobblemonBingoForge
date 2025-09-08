package io.github.adainish.cobblemonbingo.util;

import io.github.adainish.cobblemonbingo.CobblemonBingo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TextUtil
{
    private static final MutableComponent PLUGIN_PREFIX = Component.literal(Util.formattedString(CobblemonBingo.languageConfig.prefix)).setStyle(Style.EMPTY.withColor(TextColor.parseColor("#00AFFC").getOrThrow()));

    private static final MutableComponent MESSAGE_PREFIX = getPluginPrefix().append(Component.literal(CobblemonBingo.languageConfig.splitter).setStyle(Style.EMPTY.withColor(TextColor.parseColor("#FF6700").getOrThrow())));

    /**
     * @return a copy of the coloured OutBreaks TextComponent
     */
    public static MutableComponent getPluginPrefix() {
        return PLUGIN_PREFIX.copy();
    }

    /**
     * @return a copy of the coloured OutBreaks prefix
     */
    public static MutableComponent getMessagePrefix() {
        return MESSAGE_PREFIX.copy();
    }
}