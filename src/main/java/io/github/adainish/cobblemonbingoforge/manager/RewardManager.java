package io.github.adainish.cobblemonbingoforge.manager;

import io.github.adainish.cobblemonbingoforge.obj.Reward;

import java.util.HashMap;

public class RewardManager
{
    public HashMap<String, Reward> rewards = new HashMap<>();

    public RewardManager()
    {
        init();
    }

    public void init()
    {
        if (rewards.isEmpty())
        {
            for (int i = 0; i < 10; i++) {
                Reward reward = new Reward("reward_" + i);
                reward.guiDisplayOrder = i;
                rewards.put(reward.identifier, reward);
            }
        }
    }
}
