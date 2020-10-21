package ru.VirtaMarketAnalyzer.main;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;

import java.io.IOException;


public class TrendUpdaterTest {
    @Test
    void updateTrendsTest() throws IOException, GitAPIException {
        TrendUpdater.updateTrends(Wizard.realms);
    }
}
