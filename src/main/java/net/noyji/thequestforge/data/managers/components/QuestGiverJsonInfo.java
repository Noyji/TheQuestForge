package net.noyji.thequestforge.data.managers.components;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.noyji.thequestforge.TheQuestForge;
import org.slf4j.Marker;

import java.util.List;

public class QuestGiverJsonInfo {
    private final List<String> pool;

    public QuestGiverJsonInfo(List<String> pool) {
        this.pool = pool;
    }

    public String getRandomPool(RandomSource randomSource){
        return pool.get(randomSource.nextInt(pool.size()));
    }

    public boolean hasPool(String pool){
        if (pool == null || pool.isEmpty()) return false;
        return this.pool.contains(pool);
    }

    public boolean scan(Marker marker, ResourceLocation resourceLocation){
        if (pool == null || pool.isEmpty()) {
            TheQuestForge.LOGGER.debug(marker, resourceLocation + " field \"pool\" not found, or empty");
            return false;
        }
        return true;
    }
}
