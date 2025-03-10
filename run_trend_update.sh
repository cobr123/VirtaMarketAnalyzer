#!/bin/sh
set -e

export dayOfMonth=$(date +%d)
rm -rf /tmp/VirtaMarketAnalyzer/virtonomic*
java -Djava.util.Arrays.useLegacyMergeSort=true -Djava.net.preferIPv4Stack=true -Dfile.encoding=utf-8 -cp /application.jar ru.VirtaMarketAnalyzer.main.TrendUpdater > /logs/log_$dayOfMonth.txt 2> /logs/log_err_$dayOfMonth.txt
