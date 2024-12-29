package cn.harryh.arkpets.envchecker;

public class ConfirmTestCheckTask extends EnvCheckTask{
    @Override
    public String getFailureReason() {
        return "";
    }

    @Override
    public String getFailureDetail() {
        return "";
    }

    @Override
    public boolean tryFix() {
        return true;
    }

    @Override
    public boolean needConfirmFix() {
        return true;
    }

    @Override
    public boolean canFix() {
        return true;
    }

    @Override
    public boolean run() {
        return false;
    }

    @Override
    public String getFixReason() {
        return "Test 安装桥接库";
    }

    @Override
    public String getFixDetail() {
        return "为了使桌宠能与 macOS 桌面正确交互，我们需要安装一个桥接库。\n您可以在 https://github.com/litwak913/Ark-Pets-Integration 处查看源代码";
    }
}
