package io.github.adainish.cobblemonbingo.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Method;

/**
 * Tells ConquestQuests that a player did something in this mod, so quests can count it with a
 * {"type": "custom_event", "event": "<name>"} objective. ConquestQuests is optional: it is called by reflection (no
 * compile dependency), and without it this does nothing. Always runs on the server thread.
 */
public final class QuestEvents {
    private static volatile boolean resolved;
    private static Method fire;

    private QuestEvents() {
    }

    /** @param event lower case, one word: letters, digits, '_', '.', '-' (e.g. "bingo.line") */
    public static void fire(ServerPlayer player, String event) {
        if (player == null || event == null || event.isBlank()) {
            return;
        }
        Method method = method();
        if (method == null) {
            return;
        }
        Runnable call = () -> {
            try {
                method.invoke(null, player, event);
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
                // a quest event must never break the mod
            }
        };
        if (player.server.isSameThread()) {
            call.run();
        } else {
            player.server.execute(call);
        }
    }

    private static Method method() {
        if (!resolved) {
            synchronized (QuestEvents.class) {
                if (!resolved) {
                    fire = lookup();
                    resolved = true;
                }
            }
        }
        return fire;
    }

    private static Method lookup() {
        if (!FabricLoader.getInstance().isModLoaded("conquestquests")) {
            return null;
        }
        try {
            Class<?> api = Class.forName("io.github.adainish.conquestquests.api.ConquestQuestsApi", true,
                    QuestEvents.class.getClassLoader());
            return api.getMethod("fireEvent", ServerPlayer.class, String.class);
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }
}
