/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets;

import cn.harryh.arkpets.kt.model.CharData;
import cn.harryh.arkpets.kt.query.builders.CharInfoQueryBuilder;
import cn.harryh.arkpets.kt.query.builders.ModelAssetsQueryBuilder;
import cn.harryh.arkpets.kt.query.conditions.MatchConditionType;
import cn.harryh.arkpets.kt.repository.ModelRepository;
import cn.harryh.arkpets.kt.repository.RepositoryConfig;
import cn.harryh.arkpets.utils.ArgPending;
import cn.harryh.arkpets.utils.Logger;
import javafx.application.Application;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

import static cn.harryh.arkpets.Const.LogConfig;
import static cn.harryh.arkpets.Const.appVersion;


/** The entrance of the whole program, also the bootstrap for ArkHomeFX.
 * @see ArkHomeFX
 */
public class DesktopLauncher {
    public static void main(String[] args) throws ClassNotFoundException {
        // Disable assistive technologies
        System.setProperty("javax.accessibility.assistive_technologies", "");
        ArgPending.argCache = args;
        // Logger
        Logger.initialize(LogConfig.logDesktopPath, LogConfig.logDesktopMaxKeep);
        try {
            Logger.setLevel(Objects.requireNonNull(ArkConfig.getConfig()).logging_level);
        } catch (Exception ignored) {
        }
        new ArgPending(LogConfig.errorArg, args) {
            protected void process(String command, String addition) {
                Logger.setLevel(Logger.ERROR);
            }
        };
        new ArgPending(LogConfig.warnArg, args) {
            protected void process(String command, String addition) {
                Logger.setLevel(Logger.WARN);
            }
        };
        new ArgPending(LogConfig.infoArg, args) {
            protected void process(String command, String addition) {
                Logger.setLevel(Logger.INFO);
            }
        };
        new ArgPending(LogConfig.debugArg, args) {
            protected void process(String command, String addition) {
                Logger.setLevel(Logger.DEBUG);
            }
        };
        Logger.info("System", "Entering the app of DesktopLauncher");
        Logger.info("System", "ArkPets version is " + appVersion);
        Logger.debug("System", "Default charset is " + Charset.defaultCharset());

        // If requested to start the core app directly
        new ArgPending("--direct-start", args) {
            protected void process(String command, String addition) {
                EmbeddedLauncher.main(args);
                System.exit(0);
            }
        };

        if (System.getenv("VCS_DEBUG") != null) {
            Logger.setLevel(Logger.DEBUG);
            RepositoryConfig modelRepositoryConfig = new RepositoryConfig(
                    Const.RepositoryConfig.ModelRepository.repoName,
                    Const.RepositoryConfig.ModelRepository.localPath,
                    Const.RepositoryConfig.ModelRepository.remotePath,
                    Const.RepositoryConfig.ModelRepository.metadataFileName,
                    Const.RepositoryConfig.ModelRepository.metadataFileName
            );
            Long startTime = System.currentTimeMillis();
            ModelRepository.INSTANCE.initRepository(modelRepositoryConfig);
//            VoiceRepository.INSTANCE.initRepository();
            Long endTime = System.currentTimeMillis();
            Logger.info("System", "VCS_DEBUG: " + (endTime - startTime) + " ms");
            List<CharData> dataList;
            dataList = new CharInfoQueryBuilder()
                    .addSearchCondition("缪", MatchConditionType.FUZZY_MATCH)
                    .addTagCondition("Operator", "Rarity_6")
                    .addTypeCondition("Operator")
                    .buildAndGetResult();
            dataList.forEach(System.out::println);
            dataList = new CharInfoQueryBuilder()
                    .addSearchCondition("mou", MatchConditionType.FUZZY_MATCH)
                    .addTagCondition("Operator", "Rarity_6")
                    .addTypeCondition("Operator")
                    .buildAndGetResult();
            dataList.forEach(System.out::println);
            var assetMap = new ModelAssetsQueryBuilder("char_249_mlyss").buildAndGetResult();
            assetMap.forEach((k, v) -> {System.out.printf("%s: %s\n", k, v);});
            if (Objects.equals(System.getenv("VCS_DEBUG"), "TRUE")) {
                return;
            }
        }

        // Java FX bootstrap
        Application.launch(ArkHomeFX.class, args);
        Logger.info("System", "Exited from DesktopLauncher successfully");
        System.exit(0);
    }
}
