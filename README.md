# 家庭点菜 · 安卓端（Android）

与微信小程序**同功能**的家庭点菜 App，专门用于安卓手机。技术选型：**Kotlin + Jetpack Compose + Room**，
**纯本地运行、不联网、不上传任何数据**，老人小孩都能快速上手。

> 与微信小程序的关系：两个端功能一致，但数据**互相独立**。安卓端把菜品、订单、购物车都存在本机数据库（随 App 卸载清除）。
> 如果你希望安卓和小程序共用同一份数据，需要把安卓端也接入腾讯云开发 CloudBase（小程序那边的 `family-order/README.md` 第六节有改造路径），当前为离线版。

---

## 一、功能清单

| 模块 | 功能 | 对应小程序 |
| --- | --- | --- |
| 点菜（首页） | 分类筛选、菜品搜索、卡片式浏览、点 ＋ 加购、底部购物车栏、下拉改数量、去结算 | ✅ 一致 |
| 购物车 | 常驻底部悬浮栏 + 明细弹层，加减数量、一键清空 | ✅ 一致（小程序为底部悬浮） |
| 确认下单 | 自动记住昵称、订单备注、提交后清空购物车 | ✅ 一致 |
| 订单 | 「我的订单」按昵称过滤 + 「待做清单」按菜品合并统计总份数 | ✅ 一致 |
| 我的 | 当前昵称、隐蔽的管理入口、清空本地缓存、使用说明 | ✅ 一致 |
| 管理（密码） | 4 位管理密码，首次使用可设置；本地 + 应用内双校验 | ✅ 一致 |
| 菜品管理 | 新增/编辑（图片压缩到 800px / 200KB 内）、上下架、删除（同步删本地图） | ✅ 一致 |
| 分类管理 | 新增/删除分类；删除含菜品的分类时提示转移到其他分类 | ✅ 一致 |
| 订单管理 | 改状态（待制作/制作中/已完成）、删除订单 | ✅ 一致 |
| 引导 | 首次打开弹出 1 页操作引导 | ✅ 一致 |

**已按排除项严格不做**：支付、充值、会员、注册登录、微信授权、广告分享、外卖配送、地址管理、评论评分。

---

## 二、环境要求

- **Android Studio** Hedgehog (2023.1) 或更新版本
- **JDK 17**（项目 `compileSdk/targetSdk = 35`，`minSdk = 26` 即 Android 8.0）
- 一台安卓 8.0+ 手机（或模拟器）用于运行

> **不想在本机装任何开发环境？** 直接跳到「九、零环境在线出包（GitHub Actions）」——
> 由云端编译出 APK，本机无需 Android Studio / JDK / Android SDK，手机浏览器下载即可安装。

---

## 三、导入与编译

### 方式一：用 Android Studio（最简单）

1. 打开 Android Studio → `Open` → 选中本目录 `family-order-android`。
2. 首次打开会提示下载 Gradle 8.9 与依赖，**保持联网**等同步完成（依赖来自 Google Maven / Maven Central）。
3. 若弹窗提示缺少 `gradle-wrapper.jar`：点 **Use local Gradle** 或 **OK**，Android Studio 会自动补齐 wrapper；
   也可在终端执行一次 `gradle wrapper`（需本机已装 Gradle）后再打开。
4. 同步完成后点 ▶ `Run 'app'`，选择已连接的手机或模拟器即可。

### 方式二：命令行（需本机装好 Gradle 8.9）

```bash
cd family-order-android
gradle wrapper          # 仅首次：生成 gradle-wrapper.jar
./gradlew assembleDebug # 打出 debug 包
```

产物在：`app/build/outputs/apk/debug/app-debug.apk`

---

## 四、安装到手机

- **Android Studio 直连**：手机开启「开发者选项 → USB 调试」，数据线连接，点 Run 即自动安装。
- **手动安装 APK**：把 `app-debug.apk` 拷到手机，用文件管理器点击安装（需允许「未知来源」）。
- 选图使用系统**照片选择器（Photo Picker）**，无需授予存储权限，隐私更安全。

---

## 五、目录结构

```
family-order-android/
├── app/build.gradle.kts          # 模块构建脚本（依赖、SDK 版本）
├── build.gradle.kts              # 根构建脚本
├── settings.gradle.kts           # 模块声明 + 仓库
├── gradle/libs.versions.toml     # 统一依赖版本（Version Catalog）
├── gradle/wrapper/               # Gradle Wrapper 配置
├── app/src/main/
│   ├── AndroidManifest.xml       # 权限/入口声明
│   ├── java/com/family/order/
│   │   ├── FamilyOrderApp.kt     # Application + 依赖容器 + ViewModel 工厂
│   │   ├── MainActivity.kt       # 入口：底部导航 + 路由 + 首启引导
│   │   ├── data/
│   │   │   ├── local/            # Room 实体、DAO、数据库
│   │   │   ├── model/            # CartItem 等 UI 用数据类
│   │   │   └── repository/       # 仓库层（封装 DAO + 跨资源操作）
│   │   ├── util/                 # 图片压缩存储、时间/价格格式化
│   │   ├── viewmodel/            # 8 个 ViewModel
│   │   └── ui/
│   │       ├── theme/            # Material3 主题（亮/暗）
│   │       ├── components/       # 通用组件（图片、卡片、购物车栏、对话框…）
│   │       └── screen/           # 8 个页面
│   └── res/                      # 字符串、主题、图标
└── README.md
```

---

## 六、数据模型（Room，本机 SQLite）

| 表 | 字段 | 说明 |
| --- | --- | --- |
| `dishes` | id, name, category, imagePath, price, desc, status, createTime | 菜品；`imagePath` 为私有目录绝对路径（非网络 URL）；status 1 上架 / 0 下架 |
| `categories` | id, name, sort, createTime | 分类；删除含菜品的分类会整体迁移菜品 |
| `orders` | id, nickname, remark, totalNum, totalPrice, status, createTime | 订单；status 0 待制作 / 1 制作中 / 2 已完成 |
| `order_goods` | id, orderId, dishId, name, imagePath, price, num | 订单明细（快照式，改名改价不影响历史） |
| `cart` | dishId(主键), num, createTime | 购物车，仅存菜品 id + 数量，展示信息实时 join `dishes` |

> 昵称、管理密码、引导标记存于 **DataStore**（`family_order_settings`）。

**图片压缩**：相册选图后，按宽度采样解码 → 缩放至 ≤800px → JPEG 质量 85→20 逐级压到 ≤200KB，存入
`filesDir/dish-images/`。删除菜品会同步删除对应本地图（仅在 `dish-images` 目录内，安全删除）。

---

## 七、与小程序的关键差异（需你知晓）

1. **数据不互通**：安卓端是独立离线库。家里若同时用小程序和安卓，两边菜谱/订单各自一份。
   想统一请按小程序 README 第六节把安卓端也接 CloudBase。
2. **管理权限靠密码防误触**：没有登录体系，任何人拿到手机输入管理密码即可进管理端——家用足够；
   若担心，可把 App 装在带锁的平板/专用机上。
3. **下拉刷新未做**：本地数据通过 Room Flow 实时刷新，本身始终最新，故未加手动下拉刷新。
4. **暗色模式**：主题已适配系统深色模式（跟随系统），无需单独开关。

---

## 八、二次开发提示

- 改主题色：编辑 `ui/theme/Theme.kt` 的 `LightColors` / `DarkColors`。
- 加字段：在对应 Entity 加属性 → 改 DAO/Repository → 数据库 `version` 升级并写 Migration（当前 `fallbackToDestructiveMigration` 会清库重建，数据量小可接受）。
- 接云端：把 `DishRepository`/`OrderRepository`/`CartRepository` 换成调用 CloudBase SDK，ViewModel 与 UI 层基本不用改。

---

## 九、零环境在线出包（GitHub Actions）

本机没有 Android Studio / JDK 也能出包：仓库已内置 `.github/workflows/build-apk.yml`，
推送到 GitHub 后由云端 runner 自动编译出 debug APK，无需本机准备任何 Android 环境。

**该流程已实测跑通**，首次约 3–6 分钟，产出 APK 约 18 MB。

1. 在 GitHub 新建仓库，复制其地址。
2. 在工程目录执行（替换成你的仓库地址）：
   ```bash
   git remote add origin <你的仓库地址>
   git push -u origin main
   ```
   > **代理**：直连 github.com 会超时；若环境自带的 `58208` 代理对 git 返回 502，改用 `127.0.0.1:7897`：
   > ```bash
   > git -c http.proxy=http://127.0.0.1:7897 -c https.proxy=http://127.0.0.1:7897 push -u origin main
   > ```
   > 经该代理 push 有时会回显 "Everything up-to-date"（**假信号**），
   > 用 `git fetch origin && git rev-parse origin/main` 确认是否真推上去了。
   > 推送含 workflow 文件的提交，PAT 需同时具备 **Contents** 与 **Workflows** 的 Read and write 权限。
3. 打开仓库 **Actions** 页，等 `Build Debug APK` 跑完。
4. 取 APK（二选一）：
   - **Artifacts**：该次运行 → **Artifacts** → `family-order-debug-apk` —— **需登录 GitHub** 才能下载。
   - **Release 附件（推荐，手机可直接下）**：**公开**仓库的 Release 资产支持匿名下载。
     把 APK 挂到 Release 后，手机浏览器直接打开下载链接即可，无需登录、不经过电脑。
5. 按「四、安装到手机」把 apk 装到手机即可使用。

> runner 会自动装好 JDK17 / Android SDK（platform-35 + build-tools 35.0.0）/ Gradle 8.9，
> 并在构建时生成 `gradle-wrapper.jar`，你只需 push 代码，出包全自动。

### 已验证可编译的版本组合

| 组件 | 版本 |
| --- | --- |
| Kotlin（含 `org.jetbrains.kotlin.plugin.compose`） | 2.0.21 |
| KSP | 2.0.21-1.0.28 |
| Compose BOM | 2025.05.00（Compose 1.8.1） |
| AGP | 8.7.3 |
| Gradle | 8.9 |

### 编译报错排查要点

- **判断法则**：同一条错误在多个 Kotlin / Compose 版本组合下原样复现 → 基本不是版本问题，回去看报错行本身。
- `Cannot access 'val RowColumnParentData?.weight': it is internal`
  —— `Modifier.weight` 是 `RowScope` / `ColumnScope` 的**成员扩展函数**，不是顶层函数，
  不能写 `import androidx.compose.foundation.layout.weight`。删掉该 import，
  在 `Row{}` / `Column{}` 内直接用即可；抽出的子组件要声明成 `fun RowScope.Xxx()`。
- `Platform declaration clash (setXxx(...))`
  —— `var x by mutableStateOf(...) + private set` 会生成 JVM 方法 `setX()`，
  与显式 `fun setX()` 同名同签名。把显式 setter 改名 `updateXxx`，
  并同步更新 `vm::setX` 这类函数引用。
- `@Composable invocations can only happen from the context of a @Composable function`
  —— 普通 lambda（如 `onConfirm = {}`）里不能调用 `LocalContext.current`，
  需在组合作用域内先取 `val context = LocalContext.current`。

