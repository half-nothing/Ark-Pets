/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks;

import javafx.scene.layout.StackPane;

import static cn.harryh.arkpets.Const.PathConfig;
import static cn.harryh.arkpets.i18n.I18n.i18n;


public class UnzipModelsTask extends UnzipTask {
    public UnzipModelsTask(StackPane root, GuiTaskStyle style, String zipPath) {
        super(root, style, zipPath, PathConfig.tempModelsUnzipDirPath);
    }

    @Override
    protected String getHeader() {
        return i18n("task.unzip.create");
    }

    @Override
    protected String getInitialContent() {
        return i18n("task.unzip.create.detail");
    }
}
