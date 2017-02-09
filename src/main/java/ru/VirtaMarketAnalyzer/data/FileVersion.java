package ru.VirtaMarketAnalyzer.data;

import java.util.Date;

/**
 * Created by cobr123 on 09.02.2017.
 */
public final class FileVersion {
    private final Date date;
    private final String content;

    public FileVersion(final Date date, final String content) {
        this.date = date;
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }
}
