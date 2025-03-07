/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.controllers;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.ArkHomeFX;
import cn.harryh.arkpets.EmbeddedLauncher;
import cn.harryh.arkpets.concurrent.ProcessPool;
import cn.harryh.arkpets.guitasks.CheckAppUpdateTask;
import cn.harryh.arkpets.guitasks.CheckEnvironmentTask;
import cn.harryh.arkpets.guitasks.DeleteTempFilesTask;
import cn.harryh.arkpets.guitasks.GuiTask;
import cn.harryh.arkpets.envchecker.EnvCheckTask;
import cn.harryh.arkpets.utils.ArgPending;
import cn.harryh.arkpets.utils.GuiPrefabs;
import cn.harryh.arkpets.utils.Logger;
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static cn.harryh.arkpets.Const.*;
import static cn.harryh.arkpets.utils.GuiComponents.Handbook;


public final class RootModule implements Controller<ArkHomeFX> {
    public Handbook trayExitHandbook = new TrayExitHandBook();
    public ProcessPool.UnexpectedExitCodeException lastLaunchFailed;
    public GuiPrefabs.PeerNodeComposer moduleWrapperComposer;

    @FXML
    public StackPane rootContainer;
    @FXML
    public StackPane root;
    @FXML
    public StackPane body;
    @FXML
    public AnchorPane wrapper1;
    @FXML
    public AnchorPane wrapper2;
    @FXML
    public AnchorPane wrapper3;
    @FXML
    public Pane wrapper4;
    @FXML
    private Pane loadingMask;
    @FXML
    private Pane splashScreen;
    @FXML
    private ImageView splashScreenIcon;

    @FXML
    private Pane sidebar;
    @FXML
    private JFXButton menuBtn1;
    @FXML
    private JFXButton menuBtn2;
    @FXML
    private JFXButton menuBtn3;
    @FXML
    public JFXButton launchBtn;

    private ArkHomeFX app;
    private boolean checkEnd;

    @Override
    public void initializeWith(ArkHomeFX app) {
        this.app = app;
        initMenuButtons();
        initLaunchButton();
        initLaunchingStatusListener();

        app.stage.setOnShown(e -> {
            Logger.debug("Launcher", "Window on shown");
            app.stage.setIconified(false);
            app.stage.toFront();
            GuiPrefabs.fadeInWindow(app.stage, durationFast, ev -> {});
        });
        app.stage.setOnHidden(e -> Logger.debug("Launcher", "Window on hidden"));

        // Load config file.
        app.config = Objects.requireNonNull(ArkConfig.getConfig(), "ArkConfig returns a null instance, please check the config file.");
        isNewcomer = app.config.isNewcomer();
        app.config.save();
    }

    /** Pops up the splash screen in the GUI.
     * @param handler The event to be handled when the splash screen is shown.
     * @param durationIn The fade-in transition duration.
     * @param durationOut The fade-out transition duration.
     */
    public void popSplashScreen(EventHandler<ActionEvent> handler, Duration durationIn, Duration durationOut) {
        body.setVisible(false);
        GuiPrefabs.fadeInNode(splashScreen, durationIn, e -> {
            handler.handle(e);
            GuiPrefabs.fadeOutNode(splashScreen, durationOut, ev -> {
                body.setVisible(true);
                launchBtn.requestFocus();
            });
        });
    }

    /** Pops up the loading mask in the GUI to inform the user to wait for an executing task.
     * @param handler The event to be handled when the loading is shown.
     */
    public void popLoading(EventHandler<ActionEvent> handler) {
        GuiPrefabs.fadeInNode(loadingMask, durationFast, e -> {
            try {
                handler.handle(e);
            } catch (Exception ex) {
                Logger.error("Task", "Foreground loading task failed, details see below.", ex);
            }
            GuiPrefabs.fadeOutNode(loadingMask, durationFast, null);
        });
    }

    /** Runs the EmbeddedLauncher to launch the ArkPets app.
     * It will run in multi-threading mode.
     * @see EmbeddedLauncher
     */
    public void startArkPetsCore() {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws InterruptedException, ExecutionException {
                // Update the logging level arg to match the custom value of the Launcher.
                ArrayList<String> args = new ArrayList<>(Arrays.asList(ArgPending.argCache.clone()));
                args.remove(LogConfig.errorArg);
                args.remove(LogConfig.warnArg);
                args.remove(LogConfig.infoArg);
                args.remove(LogConfig.debugArg);
                String temp = switch (app.config.logging_level) {
                    case LogConfig.error -> LogConfig.errorArg;
                    case LogConfig.warn  -> LogConfig.warnArg;
                    case LogConfig.info  -> LogConfig.infoArg;
                    case LogConfig.debug -> LogConfig.debugArg;
                    default -> "";
                };
                args.add(temp);
                // Start ArkPets core.
                Logger.info("Launcher", "Launching " + app.config.character_asset);
                Logger.debug("Launcher", "With args " + args);
                Future<ProcessPool.ProcessResult> future = ProcessPool.getInstance().submit(EmbeddedLauncher.class, List.of(), args);
                // ArkPets core finalized.
                if (!future.get().isSuccess()) {
                    int exitCode = future.get().exitValue();
                    Logger.warn("Launcher", "Detected an abnormal finalization of an ArkPets thread (exit code " + exitCode + "). Please check the log file for details.");
                    lastLaunchFailed = new ProcessPool.UnexpectedExitCodeException(exitCode, future.get().processId());
                    return false;
                }
                Logger.debug("Launcher", "Detected a successful finalization of an ArkPets thread.");
                return true;
            }
        };
        Thread thread = new Thread(task);
        task.setOnFailed(e ->
                Logger.error("Launcher", "Detected an unexpected failure of an ArkPets thread, details see below.", task.getException())
        );
        thread.start();
    }

    /** Fetches a regular check-app-up-date request from the ArkPets server.
     */
    public void syncRemoteMetaInfo() {
        new CheckAppUpdateTask(app.body, GuiTask.GuiTaskStyle.HIDDEN, "auto").start();
    }

    /** Plays the exit animation, deletes temp files, then invokes {@link Platform#exit()}.
     */
    public void exit() {
        popSplashScreen(e -> {
            Logger.info("Launcher", "User close request");
            GuiPrefabs.fadeOutWindow(
                    app.stage,
                    durationNormal,
                    ev -> new DeleteTempFilesTask(app.body, GuiTask.GuiTaskStyle.HIDDEN, ".+", 24 * 3600000) {
                        @Override
                        protected void onFailed(Throwable e) {
                            Platform.exit();
                        }

                        @Override
                        protected void onSucceeded(boolean result) {
                            Platform.exit();
                        }
                    }.start()
            );
        }, durationFast, durationNormal);
    }

    private void initLaunchButton() {
        // Build environment check confirm dialog.
        JFXDialog dialog = GuiPrefabs.Dialogs.createConfirmDialog(body,
                GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_HELP_ALT, GuiPrefabs.COLOR_INFO),
                "环境检查",
                "首次运行环境检查",
                "这似乎是你第一次运行 ArkPets，我们需要对您的系统进行一些基本检查以确保桌宠能够正常运行。\n你也可以跳过检查，但可能会导致使用体验下降。",
                () -> {
                    new CheckEnvironmentTask(app.body,EnvCheckTask.getAvailableTasks(),this::launchArkPets).start();
                });
        Node cancel = ((JFXDialogLayout)dialog.getContent()).getActions().get(0);
        ((JFXButton) cancel).setOnAction(e -> {
            GuiPrefabs.Dialogs.disposeDialog(dialog);
            launchArkPets();
        });
        // Set handler for internal start button.
        launchBtn.setOnAction(e -> {
            // When request to launch ArkPets:
            if (isNewcomer && !checkEnd) {
                checkEnd = true;
                dialog.show();
                return;
            }
            launchArkPets();
        });
    }

    private void initMenuButtons() {
        // Bind the menu buttons to the corresponding modules' wrappers.
        moduleWrapperComposer = new GuiPrefabs.PeerNodeComposer();
        Button[] menuBtnList = new Button[]{menuBtn1, menuBtn2, menuBtn3};
        AnchorPane[] wrapperList = new AnchorPane[]{wrapper1, wrapper2, wrapper3};
        for (int i = 0; i < 3; i++) {
            // i = {0=Models, 1=Behavior, 2=Settings}
            final int finalI = i;
            menuBtnList[i].setOnAction(e -> moduleWrapperComposer.activate(finalI));
            menuBtnList[i].getStyleClass().setAll("menu-btn");
            moduleWrapperComposer.add(i,
                    e -> menuBtnList[finalI].getStyleClass().add("menu-btn-active"),
                    e -> menuBtnList[finalI].getStyleClass().setAll("menu-btn"),
                    wrapperList[i]
            );
        }
    }

    private void initLaunchingStatusListener() {
        ScheduledService<Boolean> ss = new ScheduledService<>() {
            @Override
            protected Task<Boolean> createTask() {
                Task<Boolean> task = new Task<>() {
                    @Override
                    protected Boolean call() throws Exception {
                        if (lastLaunchFailed != null) {
                            Exception e = lastLaunchFailed;
                            lastLaunchFailed = null;
                            throw e;
                        }
                        return false;
                    }
                };
                task.setOnFailed(e -> GuiPrefabs.Dialogs.createErrorDialog(app.body, task.getException()).show());
                return task;
            }
        };
        ss.setDelay(new Duration(2000));
        ss.setPeriod(new Duration(500));
        ss.setRestartOnFailure(true);
        ss.start();
    }

    private void launchArkPets() {
        launchBtn.setDisable(true);
        app.config.save();
        if (app.config.character_asset != null && !app.config.character_asset.isEmpty()) {
            app.popLoading(ev -> {
                try {
                    // Do launch ArkPets core.
                    startArkPetsCore();
                    Thread.sleep(1200);
                    // Show handbook in the first-run.
                    if (isNewcomer)
                        trayExitHandbook.showIfNotShownBefore(app.body);
                } catch (InterruptedException ignored) {
                } finally {
                    launchBtn.setDisable(false);
                }
            });
        }
    }
    private static class TrayExitHandBook extends Handbook {
        @Override
        public String getTitle() {
            return "使用提示";
        }

        @Override
        public String getHeader() {
            return "可以通过右键系统托盘图标来管理已启动的桌宠。";
        }

        @Override
        public String getContent() {
            return "看来你已经启动了你的第一个 ArkPets 桌宠！尽情享受 ArkPets 吧！";
        }

        @Override
        protected SVGPath getIcon() {
            return GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.SVG_HELP_ALT, GuiPrefabs.COLOR_INFO);
        }
    }
}
