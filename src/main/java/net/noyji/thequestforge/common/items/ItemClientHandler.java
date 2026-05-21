package net.noyji.thequestforge.common.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.noyji.thequestforge.client.gui.book.QuestBookGui;

@OnlyIn(Dist.CLIENT)
public class ItemClientHandler {

    public static void questBookHandler(){
        Minecraft.getInstance().setScreen(new QuestBookGui());
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F)
        );
    }
}
