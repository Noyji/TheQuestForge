package net.noyji.thequestforge.common.items.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.noyji.thequestforge.client.gui.book.QuestBookGui;
import org.jetbrains.annotations.NotNull;

public class QuestBookItem extends Item {

    public QuestBookItem(Properties properties){
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (level.isClientSide()){
            Minecraft.getInstance().setScreen(new QuestBookGui());
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F)
            );
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
