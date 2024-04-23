/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.guitasks.CheckAppUpdateTask;
import cn.harryh.arkpets.guitasks.GuiTask;
import cn.harryh.arkpets.utils.*;
import cn.harryh.arkpets.utils.GuiComponents.NamedItem;
import com.jfoenix.controls.*;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.apache.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static cn.harryh.arkpets.Const.*;


public final class SettingsModule implements Controller<ArkHomeFX> {
    @FXML
    private Pane noticeBox;
    @FXML
    private JFXComboBox<NamedItem<Float>> configDisplayScale;
    @FXML
    private JFXComboBox<NamedItem<Integer>> configDisplayFps;
    @FXML
    public JFXComboBox<NamedItem<Integer>> configCanvasSize;
    @FXML
    private JFXComboBox<String> configLoggingLevel;
    @FXML
    private Label exploreLogDir;
    @FXML
    private JFXTextField configNetworkAgent;
    @FXML
    private Label configNetworkAgentStatus;
    @FXML
    private JFXCheckBox configAutoStartup;
    @FXML
    private JFXCheckBox configSolidExit;
    @FXML
    private Label aboutQueryUpdate;
    @FXML
    private Label aboutVisitWebsite;
    @FXML
    private Label aboutReadme;
    @FXML
    private Label aboutGitHub;

    private GuiComponents.NoticeBar appVersionNotice;
    private GuiComponents.NoticeBar diskFreeSpaceNotice;
    private GuiComponents.NoticeBar fpsUnreachableNotice;

    private ArkHomeFX app;

    @Override
    public void initializeWith(ArkHomeFX app) {
        this.app = app;
        initNoticeBox();
        initConfigDisplay();
        initConfigAdvanced();
        initAbout();
        initScheduledListener();
    }

    private void initConfigDisplay() {
        new GuiComponents.ComboBoxSetup<>(configDisplayScale).setItems(new NamedItem<>("x0.5", 0.5f),
                new NamedItem<>("x0.75", 0.75f),
                new NamedItem<>("x1.0", 1f),
                new NamedItem<>("x1.25", 1.25f),
                new NamedItem<>("x1.5", 1.5f),
                new NamedItem<>("x2.0", 2f),
                new NamedItem<>("x2.5", 2.5f),
                new NamedItem<>("x3.0", 3.0f))
                .selectValue(app.config.display_scale, "x" + app.config.display_scale + "（自定义）")
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.display_scale = newValue.value();
                    app.config.saveConfig();
                });
        new GuiComponents.ComboBoxSetup<>(configDisplayFps).setItems(new NamedItem<>("25", 25),
                new NamedItem<>("30", 30),
                new NamedItem<>("45", 45),
                new NamedItem<>("60", 60),
                new NamedItem<>("120", 120))
                .selectValue(app.config.display_fps, app.config.display_fps + "（自定义）")
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.display_fps = newValue.value();
                    app.config.saveConfig();
                    fpsUnreachableNotice.refresh();
                });
        new GuiComponents.ComboBoxSetup<>(configCanvasSize).setItems(new NamedItem<>("最宽", 4),
                new NamedItem<>("较宽", 8),
                new NamedItem<>("标准", 16),
                new NamedItem<>("较窄", 32),
                new NamedItem<>("最窄", 0))
                .selectValue(app.config.canvas_fitting_samples, "每" + app.config.canvas_fitting_samples + "帧采样（自定义）")
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.canvas_fitting_samples = newValue.value();
                    app.config.saveConfig();
                });
    }

    private void initConfigAdvanced() {
        configLoggingLevel.getItems().setAll(Const.LogConfig.debug, Const.LogConfig.info, Const.LogConfig.warn, Const.LogConfig.error);
        configLoggingLevel.valueProperty().addListener(observable -> {
            if (configLoggingLevel.getValue() != null) {
                Logger.setLevel(Level.toLevel(configLoggingLevel.getValue(), Level.INFO));
                app.config.logging_level = Logger.getLevel().toString();
                app.config.saveConfig();
            }
        });
        String level = app.config.logging_level;
        List<String> args = Arrays.asList(ArgPending.argCache);
        if (args.contains(Const.LogConfig.errorArg))
            level = Const.LogConfig.error;
        else if (args.contains(Const.LogConfig.warnArg))
            level = Const.LogConfig.warn;
        else if (args.contains(Const.LogConfig.infoArg))
            level = Const.LogConfig.info;
        else if (args.contains(Const.LogConfig.debugArg))
            level = Const.LogConfig.debug;
        configLoggingLevel.getSelectionModel().select(level);

        exploreLogDir.setOnMouseClicked(e -> {
            // Only available in Windows OS
            try {
                Logger.debug("Config", "Request to explore the log dir");
                Runtime.getRuntime().exec("explorer logs");
            } catch (IOException ex) {
                Logger.warn("Config", "Exploring log dir failed");
            }
        });

        configNetworkAgent.setPromptText("示例：0.0.0.0:0");
        configNetworkAgent.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                configNetworkAgentStatus.setText("未使用代理");
                configNetworkAgentStatus.setStyle("-fx-text-fill:" + GuiPrefabs.Colors.COLOR_LIGHT_GRAY);
                Logger.info("Network", "Set proxy to none");
                System.setProperty("http.proxyHost", "");
                System.setProperty("http.proxyPort", "");
                System.setProperty("https.proxyHost", "");
                System.setProperty("https.proxyPort", "");
            } else {
                if (newValue.matches(ipPortRegex)) {
                    String[] ipPort = newValue.split(":");
                    System.setProperty("http.proxyHost", ipPort[0]);
                    System.setProperty("http.proxyPort", ipPort[1]);
                    System.setProperty("https.proxyHost", ipPort[0]);
                    System.setProperty("https.proxyPort", ipPort[1]);
                    configNetworkAgentStatus.setText("代理生效中");
                    configNetworkAgentStatus.setStyle("-fx-text-fill:" + GuiPrefabs.Colors.COLOR_SUCCESS);
                    Logger.info("Network", "Set proxy to host " + ipPort[0] + ", port " + ipPort[1]);
                } else {
                    configNetworkAgentStatus.setText("输入不合法");
                    configNetworkAgentStatus.setStyle("-fx-text-fill:" + GuiPrefabs.Colors.COLOR_DANGER);
                }
            }
        });
        configNetworkAgentStatus.setText("未使用代理");
        configNetworkAgentStatus.setStyle("-fx-text-fill:" + GuiPrefabs.Colors.COLOR_LIGHT_GRAY);

        configAutoStartup.setSelected(ArkConfig.StartupConfig.isSetStartup());
        configAutoStartup.setOnAction(e -> {
            if (configAutoStartup.isSelected()) {
                if (ArkConfig.StartupConfig.addStartup()) {
                    GuiPrefabs.DialogUtil.createCommonDialog(app.root,
                            GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_SUCCESS_ALT, GuiPrefabs.Colors.COLOR_SUCCESS),
                            "开机自启动",
                            "开机自启动设置成功。",
                            "下次开机时将会自动生成您最后一次启动的桌宠。",
                            null).show();
                } else {
                    if (ArkConfig.StartupConfig.generateScript() == null)
                        GuiPrefabs.DialogUtil.createCommonDialog(app.root,
                                GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_WARNING_ALT, GuiPrefabs.Colors.COLOR_WARNING),
                                "开机自启动",
                                "开机自启动设置失败。",
                                "无法确认目标程序的位置，其原因和相关解决方案如下：",
                                "为确保自启动服务的稳定性，直接打开的ArkPets的\".jar\"版启动器，是不支持配置自启动的。请使用exe版的安装包安装ArkPets后运行，或使用zip版的压缩包解压程序文件后运行。另外，当您使用错误的工作目录运行启动器时也可能出现此情况。").show();
                    else
                        GuiPrefabs.DialogUtil.createCommonDialog(app.root,
                                GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_WARNING_ALT, GuiPrefabs.Colors.COLOR_WARNING),
                                "开机自启动",
                                "开机自启动设置失败。",
                                "无法写入系统的启动目录，其原因可参见日志文件。",
                                "这有可能是由于权限不足导致的。请尝试关闭反病毒软件，并以管理员权限运行启动器。").show();
                    configAutoStartup.setSelected(false);
                }
            } else {
                ArkConfig.StartupConfig.removeStartup();
            }
        });

        configSolidExit.setSelected(app.config.launcher_solid_exit);
        configSolidExit.setOnAction(e -> {
            app.config.launcher_solid_exit = configSolidExit.isSelected();
            app.config.saveConfig();
        });
    }

    private void initAbout() {
        aboutQueryUpdate.setOnMouseClicked  (e -> {
            /* Foreground check app update */
            new CheckAppUpdateTask(app.root, GuiTask.GuiTaskStyle.COMMON, "manual").start();
        });
        aboutVisitWebsite.setOnMouseClicked (e -> NetUtils.browseWebpage(Const.PathConfig.urlOfficial));
        aboutReadme.setOnMouseClicked       (e -> NetUtils.browseWebpage(Const.PathConfig.urlReadme));
        aboutGitHub.setOnMouseClicked       (e -> NetUtils.browseWebpage(Const.PathConfig.urlLicense));
    }

    private void initNoticeBox() {
        appVersionNotice = new GuiComponents.NoticeBar(noticeBox) {
            @Override
            protected boolean isToActivate() {
                return isUpdateAvailable;
            }

            @Override
            protected String getColorString() {
                return GuiPrefabs.Colors.COLOR_INFO;
            }

            @Override
            protected String getIconSVGPath() {
                return GuiPrefabs.Icons.ICON_UPDATE;
            }

            @Override
            protected String getText() {
                return "ArkPets 有新版本可用！点击此处前往下载~";
            }

            @Override
            protected void onClick(MouseEvent event) {
                NetUtils.browseWebpage(Const.PathConfig.urlDownload);
            }
        };
        diskFreeSpaceNotice = new GuiComponents.NoticeBar(noticeBox) {
            @Override
            protected boolean isToActivate() {
                long freeSpace = new File(".").getFreeSpace();
                return freeSpace < diskFreeSpaceRecommended && freeSpace > 0;
            }

            @Override
            protected String getColorString() {
                return GuiPrefabs.Colors.COLOR_WARNING;
            }

            @Override
            protected String getIconSVGPath() {
                return GuiPrefabs.Icons.ICON_WARNING_ALT;
            }

            @Override
            protected String getText() {
                return "当前磁盘存储空间不足，可能影响使用体验。";
            }
        };
        fpsUnreachableNotice = new GuiComponents.NoticeBar(noticeBox) {
            @Override
            protected boolean isToActivate() {
                for (ArkConfig.Monitor i : ArkConfig.Monitor.getMonitors())
                    if (i.hz >= configDisplayFps.getValue().value())
                        return false;
                return true;
            }

            @Override
            protected String getColorString() {
                return GuiPrefabs.Colors.COLOR_WARNING;
            }

            @Override
            protected String getIconSVGPath() {
                return GuiPrefabs.Icons.ICON_WARNING_ALT;
            }

            @Override
            protected String getText() {
                return "当前设置的帧率超过了当前显示器的刷新率。";
            }
        };
    }

    private void initScheduledListener() {
        ScheduledService<Boolean> ss = new ScheduledService<>() {
            @Override
            protected Task<Boolean> createTask() {
                Task<Boolean> task = new Task<>() {
                    @Override
                    protected Boolean call() {
                        return true;
                    }
                };
                task.setOnSucceeded(e -> {
                    appVersionNotice.refresh();
                    diskFreeSpaceNotice.refresh();
                });
                return task;
            }
        };
        ss.setDelay(new Duration(5000));
        ss.setPeriod(new Duration(5000));
        ss.setRestartOnFailure(true);
        ss.start();
    }
}
