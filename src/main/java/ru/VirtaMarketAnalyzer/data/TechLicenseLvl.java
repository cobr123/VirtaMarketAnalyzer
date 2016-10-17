package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by cobr123 on 20.03.16.
 */
final public class TechLicenseLvl {
    @SerializedName("i")
    final private String techId;
    @SerializedName("l")
    final private int lvl;
    @SerializedName("awb")
    final private List<TechLicenseAskBid> askWoBid;

    public TechLicenseLvl(final String techId, final int lvl) {
        this.techId = techId;
        this.lvl = lvl;
        this.askWoBid = null;
    }

    public TechLicenseLvl(final TechLicenseLvl techLvl, final List<TechLicenseAskBid> askWoBid) {
        this.techId = techLvl.getTechId();
        this.lvl = techLvl.getLvl();
        this.askWoBid = askWoBid;
    }

    public String getTechId() {
        return techId;
    }

    public int getLvl() {
        return lvl;
    }

    public List<TechLicenseAskBid> getAskWoBid() {
        return askWoBid;
    }
}
