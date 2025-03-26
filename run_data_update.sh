#!/bin/sh
set -e

export dayOfMonth=$(date +%d)
java -cp /application.jar ru.VirtaMarketAnalyzer.main.Wizard > /logs/log_$dayOfMonth.txt 2> /logs/log_err_$dayOfMonth.txt
