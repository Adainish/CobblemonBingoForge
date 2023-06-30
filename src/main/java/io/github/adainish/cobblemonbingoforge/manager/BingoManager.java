package io.github.adainish.cobblemonbingoforge.manager;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Pokemon;
import io.github.adainish.cobblemonbingoforge.CobblemonBingoForge;
import io.github.adainish.cobblemonbingoforge.obj.Reward;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BingoManager
{
    public List<String> possibleSlotRewards = new ArrayList<>(Arrays.asList("reward_5", "reward_6", "reward_7", "reward_8"));
    public List<String> possibleCompletionRewards = new ArrayList<>(Arrays.asList("reward_1", "reward_2", "reward_3", "reward_4"));

    public List<String> blacklistedSpeciesResourceKeys = new ArrayList<>();

    public int bingoSize = 28;

    public int bingoTimerMinutes = 720;

    public BingoManager()
    {
        initDefaultBlackList();
    }

    public void initDefaultBlackList()
    {
        PokemonSpecies.INSTANCE.getSpecies().forEach(species -> {
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

    private Reward getReward(List<String> possibleRewards) {
        List<Reward> rewardList = new ArrayList<>();
        for (String s: possibleRewards) {
            if (CobblemonBingoForge.rewardsConfig.rewardManager.rewards.containsKey(s))
                for (int i = 0; i < CobblemonBingoForge.rewardsConfig.rewardManager.rewards.get(s).chance; i++) {
                    rewardList.add(CobblemonBingoForge.rewardsConfig.rewardManager.rewards.get(s));
                }
        }
        return rewardList.get(new Random(rewardList.size()).nextInt());
    }



}
