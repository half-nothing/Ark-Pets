ArkPets附加说明文档
# 命令行启动

ArkPets 的命令行启动方法。

### 前提
必须将命令行工作目录设置为程序文件所在的目录，否则可能发生各种奇怪的错误。

### 用法
`ArkPets.exe`：
```
ArkPets [--direct-start [--load-lib]] [--quiet|--warn|--info|--debug]
```
标有 * 的选项需要与`--direct-start` 同时使用。

| 选项                                           | 描述                                                                   |
|:---------------------------------------------|:---------------------------------------------------------------------|
| `--direct-start`                             | 直接启动桌宠而不打开启动器。(v1.5.1+)                                              |
| `--quiet`                                    | 以`ERROR`日志等级运行，只记录错误信息。(v2.0.0+)                                     |
| `--warn`                                     | 以`WARN`日志等级运行，记录日志和警告信息。(v2.0.0+)                                    |
| `--info`                                     | 以`INFO`日志等级运行，此为默认日志等级。(v2.0.0+)                                     |
| `--debug`                                    | 以`DEBUG`日志等级运行，记录完整的调试信息。(v2.0.0+)                                   |
| *`--load-lib <Absolute path to the library>` | 加载外部库文件（例如 RenderDoc）。(v3.5.0+)                                      |
| *`--enable-snapshot`                         | 保存预处理阶段的截图用于调试，并启用截图功能，按<kbd>Shift</kbd> + <kbd>b</kbd>以截图。(v3.5.0+) |

### 用例
```shell
cd /d D:\MyArkPets
ArkPets --direct-start --debug
```

### 提示
如果您使用的程序文件是 `.jar` 版本 ，命令行应该以形如 `java -jar ArkPets.jar` 或者 `ArkPets.jar` 开头。  
注意写完整文件名。
