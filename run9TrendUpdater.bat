call cd /D c:\IdeaProjects\VirtaMarketAnalyzerInGit\
call mvn clean
call mvn package
set currentDate=%date%
set dayOfMonth=%currentDate:~0,2%
call "%JAVA_HOME%\bin\java" -Djava.util.Arrays.useLegacyMergeSort=true -Djava.net.preferIPv4Stack=true -Djava.io.tmpdir=f:\tmp -Xmx13G -Dfile.encoding=utf-8 -cp "c:\IdeaProjects\VirtaMarketAnalyzerInGit\target\VirtaMarketAnalyzer-jar-with-dependencies.jar" ru.VirtaMarketAnalyzer.main.TrendUpdater > "c:\IdeaProjects\VirtaMarketAnalyzerInGit\logs\log_%dayOfMonth%.txt" 2>"c:\IdeaProjects\VirtaMarketAnalyzerInGit\logs\log_err_%dayOfMonth%.txt"