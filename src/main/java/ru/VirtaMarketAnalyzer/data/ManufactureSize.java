package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cobr123 on 10.11.2016.
 */
public final class ManufactureSize {
    @SerializedName("wc")
    final int workplacesCount;
    @SerializedName("me")
    final int maxEquipment;
    @SerializedName("bdw")
    final int buildingDurationWeeks;

    public ManufactureSize(final int workplacesCount, final int maxEquipment, final int buildingDurationWeeks) {
        this.workplacesCount = workplacesCount;
        this.maxEquipment = maxEquipment;
        this.buildingDurationWeeks = buildingDurationWeeks;
    }

    public int getWorkplacesCount() {
        return workplacesCount;
    }

    public int getMaxEquipment() {
        return maxEquipment;
    }

    public int getBuildingDurationWeeks() {
        return buildingDurationWeeks;
    }
}
