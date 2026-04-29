package net.noyji.thequestforge.data.group.components;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.ResourceLocation;

public class JsonTask extends JsonReward {
    @SerializedName("task_type")
    private String taskType;

    public ResourceLocation getTaskType() {
        return ResourceLocation.parse(taskType);
    }
}
