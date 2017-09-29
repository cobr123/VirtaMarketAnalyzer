module ru.VirtaMarketAnalyzer {
    requires java.base;
    requires java.datatransfer;
    requires java.desktop;
    requires java.logging;
    requires java.management;
    requires java.naming;
    requires java.rmi;
    requires java.security.jgss;
    requires java.sql;

    requires log4j;
    requires jsoup;
    requires slf4j.api;

    exports ru.VirtaMarketAnalyzer.main;
}
