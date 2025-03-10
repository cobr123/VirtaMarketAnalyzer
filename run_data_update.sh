#!/bin/sh
set -e

export dayOfMonth=$(date +%d)
java -Djava.util.Arrays.useLegacyMergeSort=true -Djava.net.preferIPv4Stack=true -Dfile.encoding=utf-8 -cp /application.jar ru.VirtaMarketAnalyzer.main.Wizard > /logs/log_$dayOfMonth.txt 2> /logs/log_err_$dayOfMonth.txt
