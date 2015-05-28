package ru.VirtaMarketAnalyzer.publish;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by r.tabulov on 06.05.2015.
 */
final public class GitHubPublisher {
    private static final Logger logger = LoggerFactory.getLogger(GitHubPublisher.class);

    public static void publish(final List<String> realms) throws IOException, GitAPIException {
        final String localPath = Utils.getDir() + "remote_repository" + File.separator;
        final File localPathFile = new File(localPath);
        final Git git = getRepo(localPathFile);
        copyToLocalRepo(localPath, realms);
        logger.info("git add .");
        git.add().addFilepattern(".").call();
        logger.info("git commit");
        git.commit().setMessage("data update").call();
        logger.info("git push");
        git.push().setCredentialsProvider(getCredentialsProvider()).call();
        git.close();
        FileUtils.deleteDirectory(localPathFile);
    }

    private static void copyToLocalRepo(final String localPath, final List<String> realms) throws IOException {
        for (final String realm : realms) {
            final File srcDir = new File(Utils.getDir() + Wizard.by_trade_at_cities + File.separator + realm + File.separator);
            if (srcDir.exists()) {
                final File destDir = new File(localPath + Wizard.by_trade_at_cities + File.separator + realm + File.separator);
                logger.info("копируем {} в {}", srcDir.getAbsolutePath(), destDir.getAbsolutePath());
                FileUtils.copyDirectory(srcDir, destDir);
            }
        }
        for (final String realm : realms) {
            final File srcDir = new File(Utils.getDir() + Wizard.industry + File.separator + realm + File.separator);
            if (srcDir.exists()) {
                final File destDir = new File(localPath + Wizard.industry + File.separator + realm + File.separator);
                logger.info("копируем {} в {}", srcDir.getAbsolutePath(), destDir.getAbsolutePath());
                FileUtils.copyDirectory(srcDir, destDir);
            }
        }
    }

    private static CredentialsProvider getCredentialsProvider() {
        final String token = System.getenv("vma.github.token");
        final String name = System.getenv("vma.github.username");
        if ((name == null || name.isEmpty()) && (token == null || token.isEmpty())) {
            throw new IllegalArgumentException("Необходимо задать логин к репозиторию (vma.github.username) или токен (vma.github.token)");
        }
        final String password = System.getenv("vma.github.password");
        if (name != null && !name.isEmpty() && (password == null || password.isEmpty())) {
            throw new IllegalArgumentException("Не задан пароль к репозиторию (vma.github.password)");
        }
        if (token != null && !token.isEmpty()) {
            logger.info("auth by token");
            return new UsernamePasswordCredentialsProvider(token, "");
        } else {
            logger.info("auth by username and password");
            return new UsernamePasswordCredentialsProvider(name, password);
        }
    }

    private static Git getRepo(final File localPathFile) throws IOException, GitAPIException {
        if (localPathFile.exists()) {
            FileUtils.deleteDirectory(localPathFile);
        }
        //"https://github.com/user/repo.git"
        final String remotePath = System.getenv("vma.github.remotepath");
        if (remotePath == null || remotePath.isEmpty()) {
            throw new IllegalArgumentException("Не задан удаленный путь к репозиторию (vma.github.remotepath), например https://github.com/user/repo.git");
        }
        logger.info("git clone {} в {}", remotePath, localPathFile.getAbsolutePath());
        final CloneCommand cloneCommand = Git.cloneRepository();
        cloneCommand.setURI(remotePath);
        cloneCommand.setDirectory(localPathFile);
        cloneCommand.setCredentialsProvider(getCredentialsProvider());
        return cloneCommand.call();
    }

    public static void main(String[] args) throws IOException, GitAPIException {
        final String localPath = Utils.getDir() + "remote_repository" + File.separator;
        final File localPathFile = new File(localPath);
        logger.info("localPath: " + localPath);
        final Git git = getRepo(localPathFile);
        logger.info("git commit");
        git.commit().setMessage("test").call();
        logger.info("git push");
        git.push().setCredentialsProvider(getCredentialsProvider()).call();
        git.close();
    }
}
