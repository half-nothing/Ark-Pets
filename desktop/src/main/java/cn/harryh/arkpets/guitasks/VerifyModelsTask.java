/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.guitasks;

import cn.harryh.arkpets.assets.AssetItem;
import cn.harryh.arkpets.assets.AssetItemGroup;
import cn.harryh.arkpets.assets.ModelsDataset;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.Logger;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import static cn.harryh.arkpets.utils.GuiPrefabs.Colors;
import static cn.harryh.arkpets.utils.GuiPrefabs.Icons;


public class VerifyModelsTask extends GuiTask {
    protected final ModelsDataset modelsDataset;
    private final Node[] dialogGraphic = new Node[1];
    private final String[] dialogHeader = new String[1];
    private final String[] dialogContent = new String[1];

    public VerifyModelsTask(StackPane root, GuiTaskStyle style, ModelsDataset modelsDataset) {
        super(root, style);
        this.modelsDataset = modelsDataset;
    }

    @Override
    protected String getHeader() {
        return "正在验证模型资源完整性...";
    }

    @Override
    protected String getInitialContent() {
        return "这可能需要数秒钟";
    }

    @Override
    protected Task<Boolean> getTask() {
        return new Task<>() {
            @Override
            protected Boolean call() {
                AssetItemGroup validModelAssets = modelsDataset.data.filter(AssetItem::isValid);
                int currentProgress = 0;
                int totalProgress = validModelAssets.size();

                boolean flag = false;
                for (AssetItem item : validModelAssets) {
                    this.updateProgress(currentProgress++, totalProgress);
                    if (this.isCancelled()) {
                        // Cancelled:
                        Logger.info("Checker", "Model repo check was cancelled in verification stage.");
                        return false;
                    } else if (!item.isChecked()) {
                        if (!item.isExisted()) {
                            // Dir missing:
                            Logger.info("Checker", "Model repo check finished (dir not integral)");
                            dialogGraphic[0] = Icons.getIcon(Icons.ICON_WARNING_ALT, Colors.COLOR_WARNING);
                            dialogHeader[0] = "已发现问题，模型资源可能不完整";
                            dialogContent[0] = "资源 " + item.assetDir + " 不存在。重新下载模型文件可能解决此问题。";
                        } else {
                            // Dir existing but file missing
                            Logger.info("Checker", "Model repo check finished (file not integral)");
                            dialogGraphic[0] = Icons.getIcon(Icons.ICON_WARNING_ALT, Colors.COLOR_WARNING);
                            dialogHeader[0] = "已发现问题，模型资源可能不完整";
                            dialogContent[0] = "资源 " + item.assetDir + " 缺少部分文件。重新下载模型文件可能解决此问题。";
                        }
                        flag = true;
                        break;
                    }
                }

                if (!flag) {
                    Logger.info("Checker", "Model repo check finished (okay)");
                    dialogGraphic[0] = Icons.getIcon(Icons.ICON_SUCCESS_ALT, Colors.COLOR_SUCCESS);
                    dialogHeader[0] = "模型资源是完整的。";
                    dialogContent[0] = "这只能说明本地的模型资源是完整的，但不一定是最新的。";
                }
                return true;
            }
        };
    }

    @Override
    protected void onFailed(Throwable e) {
        if (style != GuiTaskStyle.HIDDEN)
            GuiPrefabs.DialogUtil.createErrorDialog(root, e).show();
    }

    @Override
    protected void onSucceeded(boolean result) {
        if (style != GuiTaskStyle.HIDDEN)
            GuiPrefabs.DialogUtil.createCommonDialog(root,
                    dialogGraphic[0],
                    "验证资源完整性",
                    dialogHeader[0],
                    dialogContent[0],
                    null).show();
    }
}
