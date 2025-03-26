#!/bin/sh
set -e

export dayOfMonth=$(date +%d)
rm -rf /tmp/VirtaMarketAnalyzer/virtonomic*
java -cp /application.jar ru.VirtaMarketAnalyzer.main.TrendUpdater > /logs/log_$dayOfMonth.txt 2> /logs/log_err_$dayOfMonth.txt
