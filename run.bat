call cd /D %userprofile%\IdeaProjects\VirtaMarketAnalyzerInGit\
call mvn clean
call mvn package
set currentDate=%date%
set dayOfMonth=%currentDate:~0,2%
call "C:\Program Files\Java\jdk1.8.0_144\bin\java" -Djava.util.Arrays.useLegacyMergeSort=true -Djava.net.preferIPv4Stack=true -Djava.io.tmpdir=d:\tmp -Xmx12G -Dfile.encoding=utf-8 -cp "%userprofile%\IdeaProjects\VirtaMarketAnalyzerInGit\target\VirtaMarketAnalyzer-jar-with-dependencies.jar" ru.VirtaMarketAnalyzer.main.TrendUpdater > "%userprofile%\IdeaProjects\VirtaMarketAnalyzerInGit\logs\log_%dayOfMonth%.txt" 2>"%userprofile%\IdeaProjects\VirtaMarketAnalyzerInGit\logs\log_err_%dayOfMonth%.txt"