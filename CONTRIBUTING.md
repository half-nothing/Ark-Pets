## 贡献须知
如果您想提交一个 Pull Request, 必须遵守如下要求:
* IDE: Intellij IDEA
* 编译器: Java 17  
* 如非必要, **不要**修改 `gradle` 相关文件  

## 如何贡献
首先请fork本仓库, 然后clone到本地, 并使用IDEA打开项目  
等待gradle配置项目, 完成后运行如下命令安装git hook  
```shell
./gradlew installGitHooks
```
git hook 主要用于提交前的代码风格检查和自动格式化代码  
**不会对个人电脑产生任何危害, 请放心使用**  
没有经过格式化的PR很大可能会被拒绝合并  
此外, 请在正式提交前跑一遍构建流程
```shell
./gradlew clean distAll
```
以防止被github action自动构建拦截
