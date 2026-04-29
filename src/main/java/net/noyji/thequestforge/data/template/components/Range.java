package net.noyji.thequestforge.data.template.components;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;

public class Range {
    private int max = 0;
    private int min = 0;

    public boolean isEmpty(){
        return (max <= 0 && min <= 0);
    }

    public boolean isCorrect(){
        return (max > min);
    }

    public int getRandomInRange(RandomSource randomSource){
        return randomSource.nextIntBetweenInclusive(min, max);
    }

    public static Range ofValue(int value){
        Range range = new Range();
        range.of(value);
        return range;
    }

    public static Range ofValue(int min, int max){
        Range range = new Range();
        range.of(min, max);
        return range;
    }

    public void of(int value){
        if (value < 0) value = 0;
        max = value;
        min = value;
    }

    public void of(int min, int max){
        if (min < 0 ){
            min = 0;
        }
        if (max < 0){
            max = 1;
        }
        if (min > max){
            int h = max;
            max = min;
            min = h;
        }
        this.max = max;
        this.min = min;
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("max", this.max);
        nbt.putInt("min", this.min);
        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.max = nbt.getInt("max");
        this.min = nbt.getInt("min");
    }
}
