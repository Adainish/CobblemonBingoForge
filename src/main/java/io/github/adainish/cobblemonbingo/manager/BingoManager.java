package io.github.adainish.cobblemonbingo.manager;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Pokemon;
import io.github.adainish.cobblemonbingo.CobblemonBingo;
import io.github.adainish.cobblemonbingo.obj.Reward;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class BingoManager
{
    public String bingoGUITitle = "&6&l{player}'s Bingo Menu";
    public List<String> possibleSlotRewards = new ArrayList<>(Arrays.asList("reward_5", "reward_6", "reward_7", "reward_8"));
    public List<String> possibleCompletionRewards = new ArrayList<>(Arrays.asList("reward_1", "reward_2", "reward_3", "reward_4"));
    public List<String> possibleRowRewards = new ArrayList<>(Arrays.asList("reward_9", "reward_10", "reward_11", "reward_12"));
    public List<String> possibleColumnRewards = new ArrayList<>(Arrays.asList("reward_13", "reward_14", "reward_15", "reward_16"));
    public List<String> possibleDiagonalRewards = new ArrayList<>(Arrays.asList("reward_17", "reward_18", "reward_19", "reward_20"));
    public List<String> blacklistedSpeciesResourceKeys = new ArrayList<>();

    public int bingoSize = 28;

    public int bingoTimerMinutes = 720;

    public BingoManager()
    {
        initDefaultBlackList();
    }

    public void initDefaultBlackList()
    {
        PokemonSpecies.getSpecies().forEach(species -> {
            Pokemon pokemon = species.create(1);
            if (pokemon.isUltraBeast() || pokemon.isLegendary())
                blacklistedSpeciesResourceKeys.add(species.getResourceIdentifier().toString());
        });
    }

    public Reward getRandomSlotReward()
    {
        return getReward(possibleSlotRewards);
    }

    public Reward getRandomCompletionReward()
    {
        return getReward(possibleCompletionRewards);
    }

    public Reward getRandomRowReward()
    {
        return getReward(possibleRowRewards);
    }

    public Reward getRandomColumnReward()
    {
        return getReward(possibleColumnRewards);
    }
    public Reward getRandomDiagonalReward() {
        return getReward(possibleDiagonalRewards);
    }
    private Reward getReward(List<String> possibleRewards) {
        List<Reward> rewardList = new ArrayList<>();
        for (String s : possibleRewards) {
            Reward r = CobblemonBingo.rewardsConfig.rewardManager.rewards.get(s);
            if (r != null) {
                for (int i = 0; i < r.chance; i++) {
                    rewardList.add(r);
                }
            }
        }

        if (rewardList.isEmpty()) {
            return null;
        }

        return rewardList.get(ThreadLocalRandom.current().nextInt(rewardList.size()));
    }
}
