call cd /D %userprofile%\IdeaProjects\VirtaMarketAnalyzerInGit\
call mvn clean
call mvn package
set currentDate=%date%
set dayOfMonth=%currentDate:~0,2%
call "C:\Program Files\Java\jre1.8.0_131\bin\java" -Djava.util.Arrays.useLegacyMergeSort=true -Djava.net.preferIPv4Stack=true -Djava.io.tmpdir=d:\tmp -Xmx12G -Dfile.encoding=utf-8 -jar "%userprofile%\IdeaProjects\VirtaMarketAnalyzerInGit\target\VirtaMarketAnalyzer-jar-with-dependencies.jar" > "%userprofile%\IdeaProjects\VirtaMarketAnalyzerInGit\logs\log_%dayOfMonth%.txt" 2>"%userprofile%\IdeaProjects\VirtaMarketAnalyzerInGit\logs\log_err_%dayOfMonth%.txt"