package net.noyji.thequestforge.data.group;

import com.google.gson.annotations.SerializedName;
import net.minecraft.util.RandomSource;
import net.noyji.thequestforge.data.group.components.JsonReward;
import net.noyji.thequestforge.data.group.components.JsonTask;
import net.noyji.thequestforge.data.template.components.Range;

import java.util.List;

public class Group {
    private List<JsonTask> tasks;
    private List<JsonReward> rewards;
    @SerializedName("xp_reward")
    private Range xpReward;
    @SerializedName("currency_reward")
    private Range currencyReward;

   public List<JsonTask> getTasks(){
       return tasks;
   }

   public List<JsonReward> getRewards(){
       return rewards;
   }

    public int getXp(RandomSource randomSource){
        return xpReward.getRandomInRange(randomSource);
    }

    public int getCurrencyReward(RandomSource randomSource){
        return currencyReward.getRandomInRange(randomSource);
    }
}
