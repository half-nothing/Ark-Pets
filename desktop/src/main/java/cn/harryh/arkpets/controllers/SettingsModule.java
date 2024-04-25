/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.Const;
import cn.harryh.arkpets.guitasks.CheckAppUpdateTask;
import cn.harryh.arkpets.guitasks.GuiTask;
import cn.harryh.arkpets.i18n.I18n;
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
import static cn.harryh.arkpets.i18n.I18n.i18n;


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
    public JFXComboBox<Languages.Language> preferLanguage;
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
        new GuiComponents.ComboBoxSetup<>(configDisplayScale).setItems(
                new NamedItem<>("x0.5", 0.5f),
                new NamedItem<>("x0.75", 0.75f),
                new NamedItem<>("x1.0", 1f),
                new NamedItem<>("x1.25", 1.25f),
                new NamedItem<>("x1.5", 1.5f),
                new NamedItem<>("x2.0", 2f),
                new NamedItem<>("x2.5", 2.5f),
                new NamedItem<>("x3.0", 3.0f))
                .selectValue(app.config.display_scale, i18n("app.settings.display.zoom.tip", app.config.display_scale))
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.display_scale = newValue.value();
                    app.config.saveConfig();
                });
        new GuiComponents.ComboBoxSetup<>(configDisplayFps).setItems(
                new NamedItem<>("25", 25),
                new NamedItem<>("30", 30),
                new NamedItem<>("45", 45),
                new NamedItem<>("60", 60),
                new NamedItem<>("120", 120))
                .selectValue(app.config.display_fps, i18n("app.settings.display.fps.tip", app.config.display_fps))
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.display_fps = newValue.value();
                    app.config.saveConfig();
                    fpsUnreachableNotice.refresh();
                });
        new GuiComponents.ComboBoxSetup<>(configCanvasSize).setItems(
                new NamedItem<>(i18n("app.settings.display.border.tip.lv1"), 4),
                new NamedItem<>(i18n("app.settings.display.border.tip.lv2"), 8),
                new NamedItem<>(i18n("app.settings.display.border.tip.lv3"), 16),
                new NamedItem<>(i18n("app.settings.display.border.tip.lv4"), 32),
                new NamedItem<>(i18n("app.settings.display.border.tip.lv5"), 0))
                .selectValue(app.config.canvas_fitting_samples, i18n("app.settings.display.border.tip", app.config.canvas_fitting_samples))
                .setOnNonNullValueUpdated((observable, oldValue, newValue) -> {
                    app.config.canvas_fitting_samples = newValue.value();
                    app.config.saveConfig();
                });
    }

    private void initConfigAdvanced() {
        preferLanguage.getItems().setAll(Languages.supportLanguages);
        preferLanguage.setValue(Languages.Language.of(app.config.prefer_language));
        preferLanguage.valueProperty().addListener(observable -> {
            if (preferLanguage.getValue() != null) {
                app.config.prefer_language = Languages.Language.getName(preferLanguage.getValue());
                app.config.saveConfig();
                I18n.setLanguage(app.config.prefer_language);
                GuiPrefabs.DialogUtil.createConfirmDialog(app.root,
                        GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_HELP_ALT, GuiPrefabs.Colors.COLOR_INFO),
                        i18n("app.settings.language.change.title"),
                        i18n("app.settings.language.change.header"),
                        i18n("app.settings.language.change.content"),
                        () -> {
                            // TODO: Need to reboot app
                        }).show();
            }
        });

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

        configNetworkAgent.setPromptText(i18n("app.settings.proxy.example"));
        configNetworkAgent.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                configNetworkAgentStatus.setText(i18n("app.settings.proxy.unused"));
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
                    configNetworkAgentStatus.setText(i18n("app.settings.proxy.used"));
                    configNetworkAgentStatus.setStyle("-fx-text-fill:" + GuiPrefabs.Colors.COLOR_SUCCESS);
                    Logger.info("Network", "Set proxy to host " + ipPort[0] + ", port " + ipPort[1]);
                } else {
                    configNetworkAgentStatus.setText(i18n("app.settings.proxy.illegal"));
                    configNetworkAgentStatus.setStyle("-fx-text-fill:" + GuiPrefabs.Colors.COLOR_DANGER);
                }
            }
        });
        configNetworkAgentStatus.setText(i18n("app.settings.proxy.unused"));
        configNetworkAgentStatus.setStyle("-fx-text-fill:" + GuiPrefabs.Colors.COLOR_LIGHT_GRAY);

        configAutoStartup.setSelected(ArkConfig.StartupConfig.isSetStartup());
        configAutoStartup.setOnAction(e -> {
            if (configAutoStartup.isSelected()) {
                if (ArkConfig.StartupConfig.addStartup()) {
                    GuiPrefabs.DialogUtil.createCommonDialog(app.root,
                            GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_SUCCESS_ALT, GuiPrefabs.Colors.COLOR_SUCCESS),
                            i18n("app.settings.config.startup.title"),
                            i18n("app.settings.config.startup.success.header"),
                            i18n("app.settings.config.startup.success.content"),
                            null).show();
                } else {
                    if (ArkConfig.StartupConfig.generateScript() == null)
                        GuiPrefabs.DialogUtil.createCommonDialog(app.root,
                                GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_WARNING_ALT, GuiPrefabs.Colors.COLOR_WARNING),
                                i18n("app.settings.config.startup.title"),
                                i18n("app.settings.config.startup.fail.header"),
                                i18n("app.settings.config.startup.fail.program.content"),
                                i18n("app.settings.config.startup.fail.program.detail")).show();
                    else
                        GuiPrefabs.DialogUtil.createCommonDialog(app.root,
                                GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_WARNING_ALT, GuiPrefabs.Colors.COLOR_WARNING),
                                i18n("app.settings.config.startup.title"),
                                i18n("app.settings.config.startup.fail.header"),
                                i18n("app.settings.config.startup.fail.permission.content"),
                                i18n("app.settings.config.startup.fail.permission.detail")).show();
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
                return i18n("app.settings.check.update");
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
                return i18n("app.settings.disk");
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
                return i18n("app.settings.display.fps.over");
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
