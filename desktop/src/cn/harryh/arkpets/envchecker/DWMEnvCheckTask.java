package cn.harryh.arkpets.envchecker;


import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;


public class DWMEnvCheckTask extends EnvCheckTask {
    @Override
    public boolean run() {
        WinDef.BOOLByReference bool = new WinDef.BOOLByReference();
        WinNT.HRESULT res = DWMAPI.INSTANCE.DwmIsCompositionEnabled(bool);
        if (res.intValue() != 0)
            return false;
        return bool.getValue().booleanValue();
    }

    @Override
    public String getFailureReason() {
        return "DWM 合成未启用";
    }

    @Override
    public String getFailureDetail() {
        return "当前系统未启用 DWM 合成，这将导致桌宠背景不透明。\n请尝试切换系统主题以启用 DWM 合成，对于 Windows 7，请使用 Aero 主题。";
    }

    @Override
    public boolean tryFix() {
        return false;
    }

    @Override
    public boolean canFix() {
        return false;
    }

    private interface DWMAPI extends Library {
        DWMAPI INSTANCE = Native.load("dwmapi.dll",DWMAPI.class);

        WinNT.HRESULT DwmIsCompositionEnabled(WinDef.BOOLByReference pfEnabled);
    }
}
