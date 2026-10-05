# Sharding (several survival instances)

Optional. Without IveriumCore's network jar (or with the network disabled) CobblemonBingo behaves as a single server.
Contract: IveriumCore `docs/SHARDING.md`.

## Shared between instances
- Player bingo cards (slots, completion state, card timers) in the MongoDB collection from
  `config/CobblemonBingo/db_settings.json`.

## Per instance
- Nothing besides configs (`config/CobblemonBingo/*`), which can be synced between instances.
- Reward commands run on the instance where the slot was completed.

## Deployment
- **Must use MongoDB when sharded**: `enabled: true`, same `mongoDBURI`/`database`/`tableName` on every instance. With
  local files every instance keeps its own cards (and slots could be completed and rewarded again elsewhere); the mod
  logs a large error on the first join if that's the case.
- Documents get a `revision` field when written by a sharded instance; single servers ignore it.

## How it works
- Join: the player is "loading" until IveriumCore says the previous instance finished saving them (`whenReady`); the
  card document is then read together with its revision. While loading, `/bingo` says the data is still loading and
  captures don't count.
- Completing a slot saves the card **before** any slot/row/column/diagonal/card reward runs. When sharded that save is
  conditional on the revision; if it fails (another instance wrote first, or the database is down) the slot is not
  completed and no reward is given, and the newer data is reloaded.
- All other sharded saves are conditional too, so a stale copy can never overwrite newer cards.
- Quit: saved synchronously in the disconnect handler and dropped from the cache (also on a single server).
- The 60-second card task and the shutdown save only touch players online on this instance.

No config keys were added.
