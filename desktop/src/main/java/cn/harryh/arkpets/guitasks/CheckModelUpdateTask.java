/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks;

import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.IOUtils;
import cn.harryh.arkpets.utils.Logger;
import com.alibaba.fastjson.JSONObject;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static cn.harryh.arkpets.Const.PathConfig;
import static cn.harryh.arkpets.Const.charsetDefault;
import static cn.harryh.arkpets.i18n.I18n.i18n;


public class CheckModelUpdateTask extends FetchGitHubRemoteTask {
    public CheckModelUpdateTask(StackPane root, GuiTaskStyle style) {
        super(
                root,
                style,
                PathConfig.urlModelsData,
                PathConfig.tempDirPath + PathConfig.fileModelsDataPath,
                Const.isHttpsTrustAll,
                false);

        try {
            Files.createDirectories(new File(PathConfig.tempDirPath).toPath());
        } catch (Exception e) {
            Logger.warn("Task", "Failed to create temp dir.");
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getHeader() {
        return i18n("task.model.check");
    }

    @Override
    protected void onSucceeded(boolean result) {
        // When finished downloading the remote repo models info:
        try {
            String versionDescription;
            try {
                // Try to parse the remote repo models info
                JSONObject newModelsDataset = JSONObject.parseObject(IOUtils.FileUtil.readString(new File(PathConfig.tempDirPath + PathConfig.fileModelsDataPath), charsetDefault));
                versionDescription = newModelsDataset.getString("gameDataVersionDescription");
            } catch (Exception e) {
                // When failed to parse the remote repo models info
                versionDescription = "unknown";
                Logger.error("Checker", "Unable to parse remote model repo version, details see below.", e);
                GuiPrefabs.DialogUtil.createCommonDialog(root,
                        GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_WARNING_ALT, GuiPrefabs.Colors.COLOR_WARNING),
                        i18n("task.model.check.title"),
                        i18n("task.model.check.error"),
                        i18n("task.model.check.error.remote"),
                        null).show();
            }
            // When finished parsing the remote models info:
            // TODO do judgment more precisely
            // Compare the remote models info and the local models info by their MD5

            if (IOUtils.FileUtil.getMD5(new File(PathConfig.fileModelsDataPath)).equals(IOUtils.FileUtil.getMD5(new File(PathConfig.tempDirPath + PathConfig.fileModelsDataPath)))) {
                Logger.info("Checker", "Model repo version check finished (up-to-dated)");
                GuiPrefabs.DialogUtil.createCommonDialog(root,
                        GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_SUCCESS_ALT, GuiPrefabs.Colors.COLOR_SUCCESS),
                        i18n("task.model.check.title"),
                        i18n("task.model.check.nochange"),
                        i18n("task.model.check.nochange.content"),
                        i18n("task.model.check.nochange.detail", versionDescription)).show();
            } else {
                // If the result of comparison is "not the same"
                String oldVersionDescription;
                try {
                    // Try to parse the local repo models info
                    JSONObject oldModelsDataset = JSONObject.parseObject(IOUtils.FileUtil.readString(new File(PathConfig.fileModelsDataPath), charsetDefault));
                    oldVersionDescription = oldModelsDataset.getString("gameDataVersionDescription");
                } catch (Exception e) {
                    // When failed to parse the remote local models info
                    oldVersionDescription = "unknown";
                    Logger.error("Checker", "Unable to parse local model repo version, details see below.", e);
                    GuiPrefabs.DialogUtil.createCommonDialog(root,
                            GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_WARNING_ALT, GuiPrefabs.Colors.COLOR_WARNING),
                            i18n("task.model.check.title"),
                            i18n("task.model.check.error"),
                            i18n("task.model.check.error.local"),
                            null).show();
                }
                GuiPrefabs.DialogUtil.createCommonDialog(root,
                        GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_INFO_ALT, GuiPrefabs.Colors.COLOR_INFO),
                        i18n("task.model.check.title"),
                        i18n("task.model.check.change"),
                        i18n("task.model.check.change.content"),
                        i18n("task.model.check.change.detail", versionDescription, oldVersionDescription)).show();
                Logger.info("Checker", "Model repo version check finished (not up-to-dated)");
            }
        } catch (IOException e) {
            Logger.error("Checker", "Model repo version check failed unexpectedly, details see below.", e);
            if (style != GuiTaskStyle.HIDDEN)
                GuiPrefabs.DialogUtil.createErrorDialog(root, e).show();
        }
    }
}
