package io.github.adainish.cobblemonbingo.wrapper;

import io.github.adainish.cobblemonbingo.obj.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DataWrapper
{
    public HashMap<UUID, Player> playerCache = new HashMap<>();

    /** Online players whose data hasn't been loaded yet (sharded: waiting for the previous instance to save them). */
    public Set<UUID> loading = new HashSet<>();
}
