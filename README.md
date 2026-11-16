<p align="center">
  <img src="docs/icon.png" width="132" alt="美拍姿势库">
</p>

<h1 align="center">美拍姿势库</h1>

<p align="center"><b>告别拍照尴尬，记录每一次美好！</b></p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-5.0%2B-3DDC84?logo=android&logoColor=white" alt="Android 5.0+">
  <img src="https://img.shields.io/badge/Java-8-orange" alt="Java 8">
  <img src="https://img.shields.io/badge/version-1.0.15-blue" alt="version 1.0.15">
  <img src="https://img.shields.io/badge/license-个人免费-lightgrey" alt="个人免费">
</p>

---

## 项目简介

**美拍姿势库**是一个离线安卓姿势参考 App，给不会摆姿势、一面对镜头就容易僵住的人使用。打开软件挑一张参考图，照着画面里的人物站位、手部动作、机位高度和人物占屏比例摆好，按下快门即可。

当前版本只发布并维护安卓版，不再处理 iOS / 苹果工程与 IPA 打包。

- 当前版本：**V1.0.15**（versionCode `1015`）
- 包名：`com.poselib.app`
- 内置图库：**1760 张**姿势原图、**21 个一级分类**、23 张分类封面、8 张技巧配图
- 安装包体积：约 **302 MB**（图片全内置，因此体积较大）
- 最低系统：Android 5.0（API 21），竖屏使用

## 安装包下载

> 仅提供安卓安装包；当前版本为 V1.0.15，图片全内置，安装包约 302 MB。

- 百度网盘：[美拍姿势库](https://pan.baidu.com/s/5gZ0hq4QQPHVSmFj_XYwnnA)
- 夸克网盘：[美拍姿势库 V1.0.15](https://pan.quark.cn/s/a49b40ed7f70)

网盘链接如失效，可在 Issues 中反馈。

## 功能特性

### 看图与分类

- **快速分类 / 目录分类**双入口，默认展示快速分类；两组分类独立排序、独立隐藏，都可在标题栏恢复显示。
- 首页一行三宫格展示分类封面；分类页宫格列数可在 **2 / 3 / 4 / 5 / 6** 列之间切换，默认 2 列。
- 图片不压缩、不生成缩略图；网格按需采样解码，点开大图再加载高清，相邻图片预加载。
- 大图页支持左右横滑换图、双指缩放、双击放大/还原；未放大时横滑切图，放大后横滑为拖动查看细节。
- 大图底部支持收藏、镜像、学会、保存、分享、删除；镜像可左右翻转，方便对照镜子摆。
- 每张图下方仅保留「提示」和一句话拍摄技巧，不做难度星级和倒计时。

### 学习与数据

- 收藏、最近浏览、已学会三个独立清单；最近浏览最多保留 **20 条**。
- 每日打卡，记录今日是否来过和连续打卡天数。
- 「我的」数据页展示浏览张数、收藏总数、最常看分类、放大查看次数、累计使用时长、单次最长浏览、已学会数量。
- 活跃分由浏览、学会、连续打卡、使用时长和反馈条数共同计算；反馈意见会计入活跃分。
- 匿名统计打开次数、使用人数和大致排名；本机只保存随机匿名 ID，不收集手机号、照片或个人身份信息。

### 内容与工具

- **随机一张**：只从单人、户外场景、室内场景、照片风格、镜头角度、场景用途 6 个分类中抽取。
- **搜索**：支持按分类名和图片标题查找。
- **拍照技巧**：8 个图文主题，覆盖数码相机、安卓手机、AI 修图、参数设置、苹果手机、华为、OPPO、荣耀。
- **临拍技巧**：开拍前六步核对镜头、辅助线、氛围引导、占屏比例、机位角度、快门和后期微调。
- **AI 修图软件推荐**：侧栏默认折叠，点标题展开；条目只复制官网或下载链接，不自动跳转。
- **反馈**：文字 + 可选配图 + 可选联系方式，自动带上版本号；提交后联网上传，管理员审核通过后公开显示。
- **设置**：文字大小、深色模式、默认镜像、宫格列数全部内联展示，可一键恢复默认设置。
- 首页右下角悬浮快捷栏：侧边栏、收藏、最近浏览、设置。

## 图库分类

当前 21 个一级分类：

儿童亲子、单人、双人、场景用途、多人、室内场景、小众特色、引导话术、录像技巧、户外场景、手部动作、摄像参数、模特、泳装、照片风格、男生、艾寿花甲、身材适配、运动、通勤日常、镜头角度。

## 技术栈

| 项目 | 说明 |
|---|---|
| 开发语言 | Java 8，无 Kotlin |
| 构建方式 | 不使用 Gradle；`aapt2` + `javac` + `d8` + `zipalign` + `apksigner` 手工打包 |
| UI 实现 | 纯代码构建 View，无业务 XML 布局，无第三方 UI 库 |
| 网络请求 | `HttpURLConnection` + `org.json`，无 Retrofit / OkHttp |
| 最低版本 | Android 5.0（API 21），targetSdk 30 |

## 目录结构

```text
PoseLibApp/
├─ AndroidManifest.xml              清单：竖屏、10 个 Activity + 1 个分享 Provider
├─ build.ps1                        Windows 打包脚本：版本递增、编译、对齐、签名
├─ java/com/poselib/app/            26 个 Java 源文件
│  ├─ App / Prefs / Ui / BaseActivity
│  ├─ Library / Img / Thumbs / Sources
│  ├─ CategoryActivity / GridActivity
│  ├─ ViewerActivity / PagerView / ZoomImageView
│  ├─ SideNav / GridOverlay
│  ├─ Store / UserStore / Cloud
│  ├─ StatsActivity / TipsActivity / ShootTipsActivity
│  ├─ SearchActivity / AboutActivity
│  └─ FeedbackActivity / AdminActivity / ShareProvider
├─ res/                             五档 mipmap 图标与主题色
├─ assets/
│  ├─ library.json                  图库索引：分类树、快速分类、目录分类、随机池、总数
│  ├─ tips.json                     拍照技巧主题索引
│  ├─ lib/                          姿势原图（Git LFS）
│  ├─ covers/                       分类封面（Git LFS）
│  └─ tips/                         技巧配图（Git LFS）
├─ tools/
│  ├─ build_assets.py               扫描素材库、拷贝原图、生成索引与封面
│  ├─ make_icons.py                 图标裁切与五档尺寸输出
│  ├─ scan_src.py / scan_src.json   素材尺寸扫描数据
│  ├─ tips_pose.json                叶子分类一句话技巧
│  ├─ gen/                          封面、技巧配图、图标源图
│  ├─ version.txt / versioncode.txt 当前版本号
│  └─ poselib.keystore              签名证书（私有，不入库）
└─ docs/icon.png                    README 使用的应用图标
```

## 本地构建

### 环境要求

- Windows 10/11 + PowerShell 7
- JDK 17
- Android SDK Build-Tools：`aapt2`、`d8`、`zipalign`、`apksigner`
- `android.jar`
- Python 3 + Pillow

### 构建步骤

1. 修改 `build.ps1` 开头的本机路径，指向你的 JDK、Build-Tools 和 `android.jar`。
2. 修改 `tools/build_assets.py` 顶部的 `SRC`，指向自己的素材库根目录。
3. 重建图片资源与索引：

   ```powershell
   python tools\build_assets.py
   ```

   脚本会按目录生成分类树，原图直接拷贝到 `assets/lib`，不压缩、不生成缩略图；同时剔除超过 2000 万像素的大图和长宽比 ≥ 2.8 的超长图，并生成索引、封面和技巧配图。

4. 打包签名：

   ```powershell
   powershell -ExecutionPolicy Bypass -File build.ps1
   ```

   脚本会自动递增版本号、写入 Manifest、编译资源与 Java、生成 DEX、修正 Windows 下 assets 反斜杠路径、对齐并签名，最终输出 `美拍姿势库V<版本>.apk`。

### 替换自己的图库

把图片按「一级分类 / 二级分类 / 文件名.jpg」放入 `SRC` 目录，再在 `tools/build_assets.py` 中调整：

- `HOME`：快速分类展示项与封面顺序
- `CATALOG`：目录分类展示项与封面映射
- `RANDOM`：随机池允许的分类
- `MAX_MP` / `MAX_RATIO`：超大图与超长图剔除门槛

重新运行建库脚本即可自动生成索引和封面。

## 隐私说明

- 收藏、学会、历史、打卡和本机统计数据只保存在本机，不上传、不登录、不建账号。
- 使用人数和排名基于本机随机生成的匿名 ID 估算，不收集手机号、照片或个人身份信息。
- 反馈会提交文字、版本号和可选联系方式；联系方式仅管理员可见，反馈经审核通过后才公开显示。
- 反馈走免费公共通道，消息长期留存能力有限；普通用户只缓存已通过的公开反馈。

## 已知限制

- 使用人数与排名由公共通道内的匿名注册消息去重估算，不是服务端权威排行榜。
- 反馈通道为免费公共中转，如需永久存档和跨设备同步，需要自建后端。
- 仅支持竖屏；`targetSdk 30`。
- 部分第三方网盘或下载链接可能失效，可在 Issues 中反馈。

## 版权与免责

- 姿势图片仅用于拍照姿势学习参考，版权归原作者所有；如权利人认为内容侵权，请联系作者删除。
- 本项目不主张图片版权，请勿用于商业用途、二次打包售卖或违法场景。
- `tools/poselib.keystore`、签名证书和密钥不入库。

## 许可

- 个人免费使用，可自行编译、修改供自己使用。
- 禁止商业用途、禁止二次打包牟利、禁止上架应用商店售卖。
- 转载请注明出处。

## 更新日志

### V1.0.15

- 首页新增「快速分类 / 目录分类」双入口，两组独立排序、隐藏、恢复。
- 右下角新增侧边栏、收藏、最近浏览、设置快捷栏。
- 设置项改为内联展示，新增一键恢复默认。
- 最近浏览最多保留 20 条。
- 数据页新增全网使用人数、本人排名，反馈计入活跃分。
- 临拍技巧文案更新，补齐目录分类封面。
- 图库更新为 1760 张、21 个一级分类，剔除「现代五项」。

### V1.0.14

- 图库扩充到 1766 张、21 个分类。
- 侧栏补充 AI 修图、剪辑、GIF、拼图类工具链接。
- 新增临拍技巧页和右上角红色摄像机入口。

### V1.0.13

- 基础姿势图库版本，包含浏览、收藏、镜像、学会、保存、分享、删除、搜索、随机、打卡、反馈和技巧教程。

## 反馈

- 应用内「反馈」入口可直接提交，自动携带版本号，管理员审核后公开。
- GitHub Issue 可提交 BUG、分类建议、失效链接或使用建议。
