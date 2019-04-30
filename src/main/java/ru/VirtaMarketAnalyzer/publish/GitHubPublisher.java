package ru.VirtaMarketAnalyzer.publish;

import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.VirtaMarketAnalyzer.data.FileVersion;
import ru.VirtaMarketAnalyzer.data.RetailAnalytics;
import ru.VirtaMarketAnalyzer.main.Utils;
import ru.VirtaMarketAnalyzer.main.Wizard;

import java.io.*;
import java.util.*;

/**
 * Created by cobr123 on 06.05.2015.
 */
final public class GitHubPublisher {
    private static final Logger logger = LoggerFactory.getLogger(GitHubPublisher.class);
    public static final String localPath = Utils.getDir() + "remote_repository" + File.separator;

    public static void publishRetail(final List<String> realms) throws IOException, GitAPIException {
        if (realms.isEmpty()) {
            return;
        }
        final Git git = getRepo();
        FileUtils.copyDirectory(new File(Utils.getDir() + "img" + File.separator), new File(localPath + "img" + File.separator));
        copyToLocalRepo(Wizard.by_trade_at_cities, realms);
        copyToLocalRepo(Wizard.by_service, realms);
        copyToLocalRepo(Wizard.industry, realms);
        copyToLocalRepo(Wizard.tech, realms);
        copyToLocalRepo(Wizard.trade_guide, realms);
        copyToLocalRepo(Wizard.service_guide, realms);
        final String pattern = ".";
        logger.info("git add " + pattern);
        git.add().addFilepattern(pattern).call();
        logger.info("git commit");
        git.commit().setMessage("data update").call();
        logger.info("git push");
        git.push().setCredentialsProvider(getCredentialsProvider()).call();
        logger.info("git close");
        git.close();
    }

    public static void publishTrends(final List<String> realms) throws IOException, GitAPIException {
        final Git git = getRepo();
        copyTrendsToLocalRepo(Wizard.by_trade_at_cities, Wizard.retail_trends, realms);
        copyTrendsToLocalRepo(Wizard.industry, Wizard.product_remains_trends, realms);
        final String pattern = ".";
        logger.info("git add " + pattern);
        git.add().addFilepattern(pattern).call();
        logger.info("git commit");
        git.commit().setMessage("data update").call();
        logger.info("git push");
        git.push().setCredentialsProvider(getCredentialsProvider()).call();
        logger.info("git close");
        git.close();
    }

    public static List<FileVersion> getAllVersions(final Git git, final String file) throws IOException, GitAPIException {
        final List<FileVersion> list = new ArrayList<>();
        final Iterable<RevCommit> logs = git.log()
                .add(git.getRepository().resolve(Constants.HEAD))
                .addPath(file)
                .call();
        logger.trace("file = {}", file);

        for (final RevCommit rev : logs) {
            try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                logger.trace("Commit: {}, name: {}, id: {}", rev, rev.getName(), rev.getId().getName());
                if (getFileFromCommit(os, file, git.getRepository(), rev.getTree())) {
                    if (file.endsWith(".zip")) {
                        list.add(new FileVersion(rev.getAuthorIdent().getWhen(), Utils.readFromZip(new File(file).getName().replace(".zip", ""), os)));
                    } else {
                        list.add(new FileVersion(rev.getAuthorIdent().getWhen(), os.toString("UTF-8")));
                    }
                }
            } catch (final Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
        logger.trace("logs.count = {}", list.size());
        return list;
    }

    public static boolean getFileFromCommit(final OutputStream os, final String file, final Repository repo, final RevTree tree) throws IOException, GitAPIException {
        final TreeWalk treeWalk = new TreeWalk(repo);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathFilter.create(file));
        if (!treeWalk.next()) {
            logger.trace("Did not find expected file '" + file + "'");
            return false;
        }

        final ObjectId objectId = treeWalk.getObjectId(0);
        final ObjectLoader loader = repo.open(objectId);

        // and then one can the loader to read the file
        loader.copyTo(os);
        return true;
    }


    private static void copyTrendsToLocalRepo(final String dir, final String subDir, final List<String> realms) throws IOException {
        for (final String realm : realms) {
            final File srcDir = new File(Utils.getDir() + dir + File.separator + realm + File.separator + subDir + File.separator);
            if (srcDir.exists()) {
                final File destDir = new File(localPath + dir + File.separator + realm + File.separator + subDir + File.separator);
                if (destDir.exists()) {
                    logger.info("удаляем {}", destDir.getAbsolutePath());
                    FileUtils.deleteDirectory(destDir);
                }
                logger.info("копируем {} в {}", srcDir.getAbsolutePath(), destDir.getAbsolutePath());
                FileUtils.copyDirectory(srcDir, destDir);
            }
        }
    }

    private static void copyToLocalRepo(final String dir, final List<String> realms) throws IOException {
        for (final String realm : realms) {
            final File srcDir = new File(Utils.getDir() + dir + File.separator + realm + File.separator);
            if (srcDir.exists()) {
                final File destDir = new File(localPath + dir + File.separator + realm + File.separator);
                if (destDir.exists()) {
                    logger.info("удаляем {}", destDir.getAbsolutePath());
                    FileUtils.deleteDirectory(destDir);
                }
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

    public static Git getRepo() throws IOException, GitAPIException {
        return getRepo(new File(localPath));
    }

    public static Git getRepo(final File localPathFile) throws IOException, GitAPIException {
        return getRepo(localPathFile, 3);
    }

    public static Git getRepo(final File localPathFile, final int maxTriesCnt) throws IOException, GitAPIException {
        for (int tries = 1; tries <= maxTriesCnt; ++tries) {
            try {
                if (localPathFile.exists()) {
                    logger.info("git open");
                    final Git git = Git.open(localPathFile);
                    logger.info("git pull");
                    git.pull().setStrategy(MergeStrategy.THEIRS).call();
                    logger.info("git pull finished");
                    return git;
                } else {
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
                    final Git git = cloneCommand.call();
                    logger.info("git clone finished");
                    return git;
                }
            } catch (final IOException | GitAPIException e) {
                logger.error("Ошибка, попытка #{} из {}: {}", tries, maxTriesCnt, e.getLocalizedMessage());
                if (maxTriesCnt == tries) {
                    throw new IOException(e);
                } else {
                    Utils.waitSecond(3L * tries);
                }
            }
        }
        return null;
    }

    public static void testCommit() throws IOException, GitAPIException {
        logger.info("localPath: " + localPath);
        final Git git = getRepo();
        logger.info("git commit");
        git.commit().setMessage("test").call();
        logger.info("git push");
        git.push().setCredentialsProvider(getCredentialsProvider()).call();
        logger.info("git close");
        git.close();
    }

    public static void testPull() throws IOException, GitAPIException {
        logger.info("localPath: " + localPath);
        final Git git = getRepo();
    }

    public static void testGetAllVersions() throws IOException, GitAPIException {
        final Git git = getRepo();
        final List<FileVersion> list = getAllVersions(git, Wizard.by_trade_at_cities + "/" + "olga" + "/" + "retail_analytics_380000.json");
        logger.info("getAllVersions.size = {}", list.size());
        final Set<RetailAnalytics> set = new HashSet<>();

        for (final FileVersion fileVersion : list) {
            try {
                logger.info(fileVersion.getContent());
                final RetailAnalytics[] arr = new GsonBuilder().create().fromJson(fileVersion.getContent(), RetailAnalytics[].class);
                logger.info("RetailAnalytics[].length = {}", arr.length);
                Collections.addAll(set, arr);
            } catch (final Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
        logger.info("Set<RetailAnalytics>.size = {}", set.size());
    }

    public static void repackRepository() throws IOException, GitAPIException {
        final Git git = getRepo();
        logger.info("GC");
        git.gc().setAggressive(true).call();
        logger.info("GC done");
    }

    public static void main(String[] args) throws IOException, GitAPIException {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%r %d{ISO8601} [%t] %p %c %x - %m%n")));

        testGetAllVersions();
    }
}
