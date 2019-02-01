package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by cobr123 on 26.02.2016.
 */
public final class UnitTypeSpec {
    @SerializedName("i")
    final private String id;
    @SerializedName("c")
    final private String caption;
    @SerializedName("e")
    final private Product equipment;
    @SerializedName("rm")
    final private List<RawMaterial> rawMaterials;

    public UnitTypeSpec(final String id, final String caption, final Product equipment, final List<RawMaterial> rawMaterials) {
        this.id = id;
        this.caption = caption;
        this.equipment = equipment;
        this.rawMaterials = rawMaterials;
    }

    public String getId() {
        return id;
    }

    public String getCaption() {
        return caption;
    }

    public Product getEquipment() {
        return equipment;
    }

    public List<RawMaterial> getRawMaterials() {
        return rawMaterials;
    }
}
