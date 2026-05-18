package net.noyji.thequestforge.common.items.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class QuestCompassItem extends Item {

    public QuestCompassItem(Properties properties){
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level level, @NotNull List<Component> componentList,
                                @NotNull TooltipFlag flag) {
        super.appendHoverText(itemStack, level, componentList, flag);
    }

    public static void setTarget(ItemStack compass, UUID giverUUID){
        compass.getOrCreateTag().putUUID("giver_uuid", giverUUID);
    }

    public static void clearTarget(ItemStack compass){
        if (!compass.hasTag()) return;
        compass.getTag().remove("giver_uuid");
    }
}
