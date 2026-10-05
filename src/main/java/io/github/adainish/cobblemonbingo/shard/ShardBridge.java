package io.github.adainish.cobblemonbingo.shard;

import com.google.gson.JsonObject;
import io.github.adainish.iveriumcore.shard.Shard;
import io.github.adainish.iveriumcore.shard.SharedStore;
import net.fabricmc.loader.api.FabricLoader;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/** Optional sharding support through IveriumCore; behaves as a single server when IveriumCore isn't installed. */
public final class ShardBridge {
    private static final boolean PRESENT = detect();

    private ShardBridge() {
    }

    private static boolean detect() {
        if (!FabricLoader.getInstance().isModLoaded("iveriumcore")) {
            return false;
        }
        try {
            Class.forName("io.github.adainish.iveriumcore.shard.Shard");
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false; // an IveriumCore without sharding support
        }
    }

    public static boolean sharded() { return PRESENT && Impl.sharded(); }
    public static String serverId() { return PRESENT ? Impl.serverId() : "local"; }
    public static void whenReady(UUID player, Runnable action) { if (PRESENT) Impl.whenReady(player, action); else action.run(); }
    public static boolean isReady(UUID player) { return !PRESENT || Impl.isReady(player); }
    public static Runnable holdRelease(UUID player) { return PRESENT ? Impl.holdRelease(player) : () -> { }; }
    public static void joinLeaderGroup(String group) { if (PRESENT) Impl.joinLeaderGroup(group); }
    public static boolean isLeader(String group) { return !PRESENT || Impl.isLeader(group); }
    public static Optional<String> serverOf(UUID player) { return PRESENT ? Impl.serverOf(player) : Optional.empty(); }
    public static void subscribe(String topic, Consumer<JsonObject> handler) { if (PRESENT) Impl.subscribe(topic, handler); }
    public static void broadcast(String topic, JsonObject payload) { if (PRESENT) Impl.broadcast(topic, payload); }
    public static void send(String server, String topic, JsonObject payload) { if (PRESENT) Impl.send(server, topic, payload); }
    public static Optional<SharedStore> store() { return PRESENT ? Impl.store() : Optional.empty(); }

    /** Only loaded when IveriumCore is present. */
    private static final class Impl {
        static boolean sharded() { return Shard.get().sharded(); }
        static String serverId() { return Shard.get().serverId(); }
        static void whenReady(UUID p, Runnable a) { Shard.get().whenReady(p, a); }
        static boolean isReady(UUID p) { return Shard.get().isReady(p); }
        static Runnable holdRelease(UUID p) { return Shard.get().holdRelease(p); }
        static void joinLeaderGroup(String g) { Shard.get().joinLeaderGroup(g); }
        static boolean isLeader(String g) { return Shard.get().isLeader(g); }
        static Optional<String> serverOf(UUID p) { return Shard.get().serverOf(p); }
        static void subscribe(String t, Consumer<JsonObject> h) { Shard.get().subscribe(t, h); }
        static void broadcast(String t, JsonObject p) { Shard.get().broadcast(t, p); }
        static void send(String s, String t, JsonObject p) { Shard.get().send(s, t, p); }
        static Optional<SharedStore> store() { return Shard.get().store(); }
    }
}
