package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by cobr123 on 25.02.16.
 */
public final class UnitType {
    @SerializedName("i")
    final private String id;
    @SerializedName("c")
    final private String caption;
    @SerializedName("iu")
    final private String imgUrl;
    @SerializedName("s")
    final private List<UnitTypeSpec> specializations;

    public UnitType(final String id, final String caption, final String imgUrl, final List<UnitTypeSpec> specializations) {
        this.id = id;
        this.caption = caption;
        this.imgUrl = imgUrl;
        this.specializations = specializations;
    }

    public String getId() {
        return id;
    }

    public String getCaption() {
        return caption;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public List<UnitTypeSpec> getSpecializations() {
        return specializations;
    }

    public String getUnitTypeImgSrc() {
        if (imgUrl.equalsIgnoreCase("/img/unit_types/restaurant.gif")) {
            return imgUrl;
        } else if (imgUrl.equalsIgnoreCase("/img/unit_types/repair.gif")) {
            return imgUrl;
        } else {
            return "/img/unit_types/service_light.gif";
        }
    }
}
