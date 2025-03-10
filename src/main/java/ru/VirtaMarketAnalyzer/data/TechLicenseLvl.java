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

    public TechLicenseLvl(String techId, int lvl, List<TechLicenseAskBid> askWoBid) {
        this.techId = techId;
        this.lvl = lvl;
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
