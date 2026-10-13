# CobblemonBingo

Pokémon bingo cards for Cobblemon Conquest (Fabric 1.21.1, Cobblemon 1.8.2, mod id `cobblemonbingo`). Every player
gets a card of 28 random species. Catching a species on the card marks its slot and rolls a reward. Finishing a row,
column, diagonal or the whole card rolls extra rewards. A card expires after 12 hours (configurable) and is then
replaced by a new one. Rewards are console commands plus chat messages, picked by weight from `rewards.json`.
Storage is local JSON files or MongoDB; MongoDB is required when the server is sharded.

## Install

Requires Cobblemon, GooeyLibs (`gooeylibs` `fabric-api-repack` 3.1.0) and Fabric API. Server side only.

Optional:
- MongoDB storage: needs the MongoDB sync driver (`org.mongodb:mongodb-driver-sync` 4.11) on the classpath. It is not
  bundled in the jar. Without it the mod logs `MongoDB driver not found` and does not connect.
- IveriumCore (with its shard API): sharded cards across several survival instances. See [Sharding](#sharding).

Build: `gradle build` (Loom 1.8, Gradle 8.11 from the wrapper properties). The jar is
`build/libs/CobblemonBingo-<version>.jar`.

## How it works

- A player with no card gets a new one within 60 seconds of joining (the card task runs every 60 seconds). A player
  never gets a second card automatically: a new card only comes when every card is gone.
- A card holds `bingoSize` (default 28) different species, picked at random from Cobblemon's implemented species,
  minus `blacklistedSpeciesResourceKeys`. Forms and shininess don't matter: the slot is the species id
  (`cobblemon:pikachu`).
- Any capture of a species on the card completes that slot (`POKEMON_CAPTURED`). Each completed slot rolls one
  **slot** reward. A capture counts on every card the player holds.
- Every 60 seconds expired cards are removed for online players. A card expires `bingoTimerMinutes` (default 720)
  after it was created, finished or not. The timer runs while the player is offline.

### Lines

Lines are worked out on the card sorted by National Dex number, 7 slots per row (4 rows by 7 columns). Slot n (1-28)
is in row `ceil(n / 7)` and column `((n - 1) mod 7) + 1`. Diagonals are slots 1, 9, 17, 25 and 7, 13, 19, 25. This
is not the layout the card menu shows.

After a slot is completed the mod checks the whole card:

| Check | Reward list |
|---|---|
| Always | `possibleSlotRewards` |
| Any full row or any full column | `possibleRowRewards` ("Line" in the menu) |
| Any full column | `possibleColumnRewards` (once, even if several columns are full) |
| Either diagonal full | `possibleDiagonalRewards` |
| Every slot completed | `possibleCompletionRewards` |

A full column therefore gives both a line and a column reward. See [Gotchas](#gotchas) for repeats.

## Commands and permissions

There are no permission nodes. Staff commands need vanilla op level 4 (console has it). `/bingo` and
`/bingo rewards` are open to everyone.

| Command | Permission | What |
|---|---|---|
| `/bingo` | everyone | Opens your card, or the card list when you have several. Says so when you have none yet |
| `/bingo rewards` | everyone | Opens the rewards overview (players only) |
| `/bingo reload` | op 4 | Re-reads all four config files |
| `/bingo reset` | op 4 | Clears your own cards (players only) |
| `/bingo givecard <player>` | op 4 | Adds an extra card to an online player and saves |
| `/bingo resetplayer <player>` | op 4 | Clears an online player's cards. Without arguments it prints a warning |

`reset` and `resetplayer` don't ask for confirmation and don't save straight away. The next card task (within 60
seconds) gives a fresh card and saves, or the logout save stores the empty list.

When `resetplayer` can't find the player's data (still loading or failed to load), it writes a fresh empty record
over the stored one: it replaces the MongoDB document, or deletes and recreates the JSON file. The player has to
rejoin to load it.

## Menus

- **Card**: the card's species sorted by National Dex number, with a Rewards chest button. Completed slots glow and
  say `Completed!`, open slots say which species to catch. Title: `bingoGUITitle`.
- **Card list**: only when a player holds more than one card. One enchanted book per card, previous/next page arrows
  and a Rewards button.
- **Rewards**: five categories (Per Pokémon, Line, Column, Diagonal, Full Card) with the number of possible rewards.
  A category opens a paged list sorted by `guiDisplayOrder`. Each reward shows its icon, `prettyTitle`, `lore` and its
  chance in that category (its weight / the category's total weight). Read only. Title: `rewardsGUITitle`.

## Files (`config/CobblemonBingo/`)

Each file is written with defaults on the first start if it is missing. Existing files are never rewritten.

| File | What |
|---|---|
| `config.json` | Card size, timer, menu titles, reward lists, species blacklist |
| `rewards.json` | Reward definitions |
| `language.json` | Chat prefix |
| `db_settings.json` | MongoDB storage |
| `playerdata/<uuid>.json` | Player cards (file storage only) |

### `config.json`

All keys sit under `bingoManager`.

| Key | Default | Meaning |
|---|---|---|
| `bingoGUITitle` | `&6&l{player}'s Bingo Menu` | Card menu title. `{player}` is the player name |
| `rewardsGUITitle` | `&6&lBingo Rewards` | Rewards menu title |
| `possibleSlotRewards` | `reward_5` .. `reward_8` | Reward ids rolled for every completed slot |
| `possibleRowRewards` | `reward_9` .. `reward_12` | Reward ids rolled for a full row or column |
| `possibleColumnRewards` | `reward_13` .. `reward_16` | Reward ids rolled for a full column |
| `possibleDiagonalRewards` | `reward_17` .. `reward_20` | Reward ids rolled for a full diagonal |
| `possibleCompletionRewards` | `reward_1` .. `reward_4` | Reward ids rolled for a full card |
| `blacklistedSpeciesResourceKeys` | every legendary and Ultra Beast | Species ids (`cobblemon:mewtwo`) never put on a card |
| `bingoSize` | `28` | Species per card |
| `bingoTimerMinutes` | `720` | Minutes a card lives |

The default blacklist is built when the file is first written, from the species that have the `legendary` or
`ultra_beast` label. Mythicals, paradox Pokémon and species that don't spawn are not excluded.

Changes apply to new cards only. Existing cards keep their species.

### `rewards.json`

`rewardManager.rewards` maps a reward id to a reward. The defaults are `reward_0` .. `reward_9` (dirt icon,
`broadcast test`, weight 100).

| Key | Default | Meaning |
|---|---|---|
| `identifier` | the id | Reward id. Shown as the name when `prettyTitle` is null |
| `icon` | dirt | Item shown in the rewards menu (`{"id": "minecraft:diamond", "count": 1}`). Null or unknown items become paper |
| `prettyTitle` | `&b&lExample Rewards` | Name in the rewards menu |
| `lore` | `&eExample of a rewards` | Lore in the rewards menu |
| `commands` | `broadcast test` | Console commands. `%pl%` is the player name |
| `messages` | `&eYou've received an example rewards` | Chat lines sent to the player (with the prefix) |
| `guiDisplayOrder` | `0` .. `9` | Sort order in the rewards menu |
| `chance` | `100` | Weight, not a percentage. A reward is entered `chance` times (rounded up), so `0` never drops |

Write every key for a new reward. A missing key is empty, not the default above: without `chance` the reward never
drops, and without `commands` or `messages` paying it out fails (logged as a reward setup error).

### `language.json`

| Key | Default | Meaning |
|---|---|---|
| `prefix` | `&6[&bBingo&6]` | Chat prefix |
| `splitter` | ` &e» ` | Text between the prefix and the message |

Only `&` colour codes. The prefix is read once, the first time a message is sent: changes need a restart.

### `db_settings.json`

| Key | Default | Meaning |
|---|---|---|
| `enabled` | `false` | Store cards in MongoDB instead of `playerdata/` |
| `mongoDBURI` | `""` | Connection string |
| `database` | `mydatabase` | Database name |
| `tableName` | `player_data` | Collection name |

The connection is made once on server start. `/bingo reload` re-reads the file but does not reconnect.

## Rewards

Each check picks one reward from its list by weight. Ids in a list that don't exist in `rewards.json` are skipped,
and an empty list gives nothing. The reward's commands run as the console, then its messages go to the player.

## Storage

- Files: `config/CobblemonBingo/playerdata/<uuid>.json` with `uuid`, `userName` and `bingoCardList` (each card has
  `startedAt`, `selectedBingoPokemon` with `resourceKey`/`completed`/`dexNumber` per species).
- MongoDB: one document per player in the configured collection, keyed by `uuid`, same shape. Sharded writes add a
  `revision` field.
- Saved: on every completed slot (before rewards), every 60 seconds for online players, on `givecard`, on logout and
  on shutdown (online players only).
- Data is loaded on join. Until it is loaded `/bingo` says it is still loading and captures don't count.

## Sharding

Optional, through IveriumCore's shard API. Details in `SHARDING.md`. In short:
- Use MongoDB with the same URI, database and collection on every instance. With file storage each instance keeps its
  own cards; the mod logs a large error on the first join.
- Join waits until the previous instance has saved the player. Every sharded write is conditional on the stored
  `revision`; a completed slot is saved before its rewards, and when that save fails the slot stays open and no reward
  is given.
- Reward commands run on the instance where the slot was completed. No config keys.

## Quest events

When ConquestQuests is installed, the mod reports these events for `{"type": "custom_event", "event": "<name>"}`
quest objectives (no hard dependency; without ConquestQuests nothing happens). They fire on a capture that completes
a slot, after the slot was saved (a failed save fires nothing), for an online player.

| Event | Fires when |
|---|---|
| `bingo.slot` | A slot is completed (once per card the capture completes a slot on) |
| `bingo.line` | A row, column or diagonal becomes full. Once per line that was not full before this capture, so a slot finishing two lines fires it twice. Unlike the line rewards it does not repeat on later slots |
| `bingo.line.row` | Same, rows only (4 rows of 7) |
| `bingo.line.column` | Same, columns only (7 columns of 4) |
| `bingo.line.diagonal` | Same, diagonals only (2) |
| `bingo.card` | The last open slot of a card is completed |

Lines use the same Dex-sorted layout as the line rewards (see [Lines](#lines)).

## Gotchas

- **Repeat line rewards**: the row, column and diagonal checks look at the whole card, not at the line the new slot
  finished. Once any row or column is full, every later completed slot on that card rolls another line reward (and
  another column reward if a column is full, another diagonal reward if a diagonal is full).
- **Card menu**: Pokémon fill a 5 by 5 area (25 slots) and the page arrows are disabled. With the default 28 species
  the three highest Dex numbers on the card can't be seen. The menu is also built with 7 rows while a chest has at most
  6, so the Rewards button on the last row may not show; `/bingo rewards` always works.
- **Line maths assume 28**: rows are 1-4 and columns 7 wide. Other `bingoSize` values break lines and diagonals.
  A `bingoSize` larger than the number of allowed species never finishes generating a card.
- **Default reward ids don't match**: the default `rewards.json` has `reward_0` .. `reward_9`, but the default lists
  use `reward_1` .. `reward_20`. Out of the box the line list only has `reward_9`, and column and diagonal rewards are
  empty.
- **No working database, no saves**: with `enabled: true` and no connection, nothing is saved (no fallback to files).
- Card timers are wall clock. A finished card stays until it expires; no new card comes before that.
- `%pl%` is the name of the online player. Messages to offline players are dropped.
- `freeSlot` in card data is not used.
