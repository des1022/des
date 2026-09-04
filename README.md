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
