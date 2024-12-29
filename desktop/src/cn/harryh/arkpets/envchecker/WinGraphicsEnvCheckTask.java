package cn.harryh.arkpets.envchecker;

import cn.harryh.arkpets.utils.IOUtils;
import cn.harryh.arkpets.utils.Logger;
import cn.harryh.arkpets.utils.NVAPIWrapper;

import java.io.BufferedReader;


public class WinGraphicsEnvCheckTask extends EnvCheckTask {
    private String failureReason;
    private String failureDetail;
    private FixMode fix;

    public static final String NVAPI_PROFILE_NAME = "ArkPets";

    private enum FixMode {
        WIN_SAV,     // Windows Power-saving
        NV,          // NVIDIA OpenGL GDI and Present method
        WIN_PERF_NV, // Windows Performance, NVIDIA OpenGL GDI and Present method
        FAIL         // Can't Fix
    }

    @Override
    public String getFailureReason() {
        return failureReason;
    }

    @Override
    public String getFailureDetail() {
        return failureDetail;
    }

    @Override
    public boolean tryFix() {
        return false;
    }

    @Override
    public boolean canFix() {
        return fix != FixMode.FAIL;
    }

    @Override
    public boolean run() {
        String cards = wmicCheck();
        if (cards != null) {
            if (cards.contains("Intel") && cards.contains("NVIDIA")) {
                // I+N Hybrid
                // checkWinGraphicCard(false);
                fix=FixMode.WIN_SAV;
                return false;
            } else if (cards.contains("AMD") && cards.contains("NVIDIA")) {
                // A+N Hybrid
                // checkWinGraphicCard(true);
                // checkNVAPI();
                fix=FixMode.WIN_PERF_NV;
                return false;
            } else if (cards.contains("AMD")) {
                // A+A Hybrid or AMD single,Fail temporary
                fix=FixMode.FAIL;
                failureReason = "AMD 显卡警告";
                failureDetail = "检测到正在使用 AMD 显卡，ArkPets 尚未对 AMD 显卡进行充分测试。\n你仍可以强制运行，但桌宠背景可能会不透明。";
                return false;
            } else if (cards.contains("Intel") || cards.contains("NVIDIA")) {
                // Intel or NVIDIA Single card
                return true;
            } else {
                // Other card (Virtual,Software,Non-mainstream...)
                failureReason = "未知显卡警告";
                failureDetail = "当前可能正在使用特殊显卡（虚拟显卡、软件渲染等），ArkPets 尚未对这类显卡进行测试。\n你仍可以强制运行，但可能会产生未知的问题。";
                fix = FixMode.FAIL;
                return false;
            }
        } else {
            failureReason = "获取显卡信息失败";
            failureDetail = "暂时无法获取显卡信息，请参考“常见问题解答”对显卡进行设置。";
            fix = FixMode.FAIL;
            return false;
        }
    }

    private String wmicCheck() {
        try {
            String result = IOUtils.CommandUtil.runCommand("wmic path win32_VideoController get Name",null,null);
            if (result != null){
                return result;
            } else {
                Logger.warn("EnvCheck", "Failed to get graphics card info");
                return null;
            }
        } catch (Exception e) {
            Logger.warn("EnvCheck", "Failed to get graphics card info");
            return null;
        }
    }

    private void setGraphicCard(boolean performance) {

    }

    private void setNvidiaSettings() {
        NVAPIWrapper.NvAPI_Initialize();
        //TODO
        NVAPIWrapper.NvAPI_Unload();
    }

    private void checkNvidiaSettings() {
        NVAPIWrapper.NvAPI_Initialize();
        //TODO
        NVAPIWrapper.NvAPI_Unload();
    }

}
