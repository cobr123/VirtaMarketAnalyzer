package ru.VirtaMarketAnalyzer.data;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by cobr123 on 25.04.2015.
 */
public final class UpdateDate {
    @SerializedName("d")
    final private String date;

    public UpdateDate(final String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

}
