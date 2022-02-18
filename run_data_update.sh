export dayOfMonth=$(date +%d)
export vma_github_remotepath=https://github.com/cobr123/cobr123.github.com.git
export vma_github_token=
export vma_login=
export vma_password=
java -Djava.util.Arrays.useLegacyMergeSort=true -Djava.net.preferIPv4Stack=true -Djava.io.tmpdir=./tmp -Xmx12G -Dfile.encoding=utf-8 -cp ./target/VirtaMarketAnalyzer-jar-with-dependencies.jar ru.VirtaMarketAnalyzer.main.Wizard > ./logs/log_$dayOfMonth.txt 2> ./logs/log_err_$dayOfMonth.txt
