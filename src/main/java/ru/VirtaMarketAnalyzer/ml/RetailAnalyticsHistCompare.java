package ru.VirtaMarketAnalyzer.ml;

import ru.VirtaMarketAnalyzer.data.RetailAnalytics;

import java.util.Comparator;

/**
 * Created by cobr123 on 02.03.2016.
 */
public final class RetailAnalyticsHistCompare implements Comparator<RetailAnalytics> {
    @Override
    public int compare(final RetailAnalytics o1, final RetailAnalytics o2) {
        int result = 0;

        if (o1.getSellVolumeAsNumber() < o2.getSellVolumeAsNumber()) {
            result = 1;
        } else if (o1.getSellVolumeAsNumber() > o2.getSellVolumeAsNumber()) {
            result = -1;
        } else if (o1.getPrice() < o2.getPrice()) {
            result = 1;
        } else if (o1.getPrice() > o2.getPrice()) {
            result = -1;
        }

        return result;
    }
}
