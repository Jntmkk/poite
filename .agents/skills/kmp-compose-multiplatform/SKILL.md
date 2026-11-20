---
name: kmp-compose-multiplatform
description: poite 项目的 KMP + Compose Multiplatform 开发规范——UI 与逻辑全共享（commonMain 写 Compose UI，Android/iOS 各为薄壳）。创建、修改或评审 shared 模块（commonMain/androidMain/iosMain）、androidApp 或 iosApp 代码时使用。
---

# poite — KMP + Compose Multiplatform 全共享

本项目 UI 和逻辑都共享：`commonMain` 里的 Compose UI 同时跑在 Android 和 iOS 上，androidApp / iosApp 只是薄壳。已定案，不要再建议「原生 UI 更贴合平台」之类的替代架构。

改编自 [felipechaux/kmp-compose-multiplatform-skill](https://github.com/felipechaux/kmp-compose-multiplatform-skill)（MIT），按本项目实际配置与实测踩坑重写。

## 项目事实（以此为准，不要臆测）

- 包名：`net.coolshell.poite`（shared 模块 `net.coolshell.poite.shared`，framework 模块名 `shared`）
- 模块：`:shared`（KMP library，**含全部 UI 与逻辑**）、`:androidApp`（薄壳，仅 `setContent { App() }`）、`iosApp/`（手写维护的 Xcode 工程，SwiftUI 壳通过 `UIViewControllerRepresentable` 嵌入 `MainViewControllerKt.MainViewController()`）
- 版本：Kotlin 2.4.21 · CMP 1.11.1 · AGP 8.10.0 · Gradle 8.13 · compileSdk 36 / minSdk 24（**别升 CMP 1.12+**：其 androidx.compose 1.12 依赖要求 compileSdk 37，本机 SDK 只装到 android-36.1）
- 所有依赖版本集中在 `gradle/libs.versions.toml`，**禁止在 build 文件里硬编码版本号**
- shared 目标：`androidTarget` + `iosX64/iosArm64/iosSimulatorArm64`，静态 framework（`baseName = "shared"`, `isStatic = true`）
- compose resources：`compose.resources { packageOfResClass = "net.coolshell.poite.shared.generated.resources"; generateResClass = always }`，资源放 `shared/src/commonMain/composeResources/`
- iOS 集成：Xcode Run Script 调 `./gradlew :shared:embedAndSignAppleFrameworkForXcode`；签名 Team `YY4YZVH2S9`（个人证书，7 天过期需重装）
- 本机 Xcode 是 27.x：**没有独立 Simulator.app**，模拟器窗口用 Device Hub（`Contents/Applications/DeviceHub.app`，File → New Window 开设备窗口）
- kotlinx-datetime 已升 0.7.1（CMP material3 传递依赖会强制升级）：`Clock` 改用 `kotlin.time.Clock`，旧的 `kotlinx.datetime.Clock` 在 iOS 编译直接挂

常用命令：

```bash
./gradlew :androidApp:installDebug          # Android 真机/模拟器
./gradlew :shared:testDebugUnitTest         # 共享单测
./gradlew :shared:compileKotlinIosSimulatorArm64   # 快速验证 iOS 编译
# PostHog/其他 SwiftPM 依赖变更后重跑一次（会改 pbxproj，生成物要提交）：
XCODEPROJ_PATH="$PWD/iosApp/iosApp.xcodeproj" ./gradlew :shared:integrateLinkagePackage
# iOS 完整构建：xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug \
#   -destination 'platform=iOS Simulator,name=iPhone 17' CODE_SIGNING_ALLOWED=NO build
# 或直接 Xcode 打开 iosApp.xcodeproj ⌘R；不要手动跑 embedAndSign（它需要 Xcode 环境变量）
```

## 协作流程（用户定的规则）

**UI 改动必须先出线框图**（文字/ASCII 示意大概样式与布局），用户确认后才动代码；用户明确要求「完全自动化执行」时除外。非 UI 改动不受此限。

## 核心原则

1. **最大化共享**——UI、ViewModel、逻辑、数据全在 `commonMain`；平台差异走 expect/actual
2. **Clean Architecture**：Data → Domain → Presentation，按 feature 分包
3. **单向数据流**：`StateFlow<UiState>` 下行，事件经回调/SharedFlow 上行
4. **接口优先**：domain 定义接口，data 实现，DI 注入
5. **平台对等**：两端像素级一致是默认行为；平台专属样式必须显式声明理由

## shared 模块结构

```
shared/src/
├── commonMain/kotlin/net/coolshell/poite/shared/
│   └── feature/<name>/
│       ├── data/        # local(Room DAO/Entity)、remote(Ktor)、repository 实现、mapper
│       ├── domain/      # model（纯 Kotlin）、repository 接口、usecase（operator fun invoke）
│       ├── presentation/
│       │   ├── ui/        # Composable 页面与组件
│       │   ├── viewmodel/ # 共享 ViewModel
│       │   └── state/     # UiState data class
│       └── di/          # 该 feature 的 Koin module
│   ├── core/            # 横向基础设施（见下「已就位的基础设施」）
│   │   ├── logging/     # AppLogger 接口 + Kermit 实现
│   │   ├── cache/       # KvStore（Settings）+ MemoryCache（cache4k）
│   │   ├── tracking/    # Tracker 接口 + PostHog 实现（已接入）
│   │   └── crash/       # CrashReporter 接口 + Sentry 实现（已接入）
│   └── App.kt           # 根 Composable
├── androidMain/         # Android actual（Context、OkHttp、Timber…）
├── iosMain/             # iOS actual（Darwin、os_log…）+ MainViewController()
└── commonTest/          # 共享单测（kotlin.test + runTest）
```

## Presentation 层范式

```kotlin
class HomeViewModel(private val getItems: GetItemsUseCase) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // 一次性事件（导航/Toast）——绝不放进 UiState，replay 必须为 0
    private val _events = MutableSharedFlow<HomeEvent>(extraBufferCapacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    fun loadItems() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val r = getItems()) {
                is Resource.Success -> _uiState.update { it.copy(isLoading = false, items = r.data) }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = r.error.toUserMessage()) }
            }
        }
    }

    fun onCleared() = scope.cancel()
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val items: List<Item> = emptyList(),
    val errorMessage: String? = null,   // 人类可读，绝不放 AppError
)
```

Composable 页面：用 `collectAsStateWithLifecycle()`（`androidx.lifecycle:lifecycle-runtime-compose` KMP 版）收集状态；一次性事件在 `LaunchedEffect(Unit)` 里 collect。嵌套 Composable 只接收 state + 回调，不直接碰 ViewModel。

异步结果统一：

```kotlin
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val error: AppError) : Resource<Nothing>()
}
```

## 平台壳的写法（已就位，改动时照此核对）

**Android**（androidApp/MainActivity.kt）：`setContent { App() }`，无需其他 compose 依赖（activity-compose 即可）。

**iOS**：

```kotlin
// shared/iosMain/MainViewController.kt
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
```

```swift
// iosApp/ContentView.swift
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
struct ContentView: View {
    var body: some View { ComposeView().ignoresSafeArea() }
}
```

## iOS 图标与启动屏（已就位）

- **图标**：`Assets.xcassets/AppIcon.appiconset/AppIcon-1024.png`，单张 1024 全出血方形（系统自己切圆角，不要预切/加透明）。Contents.json 只需 universal + 1024x1024 一条。显示名用 Info.plist 的 `CFBundleDisplayName = poite`（否则显示默认 PRODUCT_NAME）
- **启动屏**：Info.plist `UILaunchScreen` 里 `UIImageName = LaunchLogo`（白底居中 K 标，资源在 `LaunchLogo.imageset`，1x/2x/3x = 160/320/480px）。系统静态启动屏，App 首帧就绪后自动切主页面，不需要代码控制；想换海报就换这个 imageset
- 图标/启动屏图用 Python PIL 按 `compose-multiplatform.xml` 的矢量坐标生成（深色 #041619、橙 #f98e25），脚本思路：4x 超采样 + LANCZOS 缩放

## iOS Info.plist 强制项（少了就崩/黑边，实测踩过）

1. **`CADisableMinimumFrameDurationOnPhone = true`**——CMP ≥1.7 启动时 `PlistSanityCheck` 校验，缺失直接 SIGABRT 闪退（ProMotion 帧率支持）
2. **`UILaunchScreen`（至少空 dict）**——Xcode 27 / iOS 26 起老的 `UILaunchScreen_Generation` 失效，缺有效启动屏配置的 App 会被塞进带圆角黑边的兼容画框（上下黑条、内容不铺满）

## iOS 互操作（平台 API 仍需要）

UI 共享后互操作面缩小，但 expect/actual 和平台能力调用仍在：

- 顶层函数在 Swift 侧带 `Kt` 后缀（`NewYearKt.daysPhrase()`）；`object` → `.shared`；`Unit` → `KotlinUnit`（避免在公开 API 返回）
- **Kotlin 默认参数不会桥接到 Swift**——Swift 调用侧必须传全部参数。给 Swift 的入口函数在 iosMain 包一层无参包装（如 `IosKoinKt.initKoinIos()`），不要让 Swift 直接调带默认参数的 common 函数
- multiplatform-settings 1.3.0 起一批改名：`AndroidSettings`→`SharedPreferencesSettings`、iOS 工厂 `Settings()`→`NSUserDefaultsSettings(...)`、测试的 `MockSettings`→`MapSettings`；网上老教程的写法都会编译不过
- suspend/Flow 给 Swift 用：SKIE（推荐，`co.touchlab.skie`）或 iosMain 手写回调包装；Swift 侧回调用 `[weak self]`，deinit 里取消观察
- 公开 API 避免泛型（SKIE 对泛型 sealed class 无法穷举化）；内部类型标 `@HiddenFromObjC`
- 热路径减少 Kotlin↔Swift 跨边界调用（批量取数据）
- Kotlin/Native 新内存模型：跨线程共享可变状态自行同步；UI 更新回 `Dispatchers.Main`
- 改了 framework 名或模块名，四处同步改：Xcode Run Script、`FRAMEWORK_SEARCH_PATHS`、`OTHER_LDFLAGS -framework shared`、`gradle.properties` 的 `xcodeproj=./iosApp`

## 边到边与 insets

- Android targetSdk 36 强制 edge-to-edge：根布局必须消费 insets。**Header 顶部用 `WindowInsets.safeDrawing.only(Top)`（含挖孔/刘海，别只用 statusBarsPadding）**；底部胶囊用 `navigationBarsPadding()`；内容区底部留 ~120dp 避免被悬浮胶囊遮挡
- iOS 侧 `ComposeView().ignoresSafeArea()` + Compose 内部的 safeContentPadding 配合，不需要额外处理

## 资源与 i18n（compose resources）

- 字符串：`commonMain/composeResources/values/strings.xml`，代码里 `stringResource(Res.string.xxx)`；复数用 `pluralStringResource()`，禁止 `if (count == 1)` 分支
- 图片：`composeResources/drawable/xxx.xml`（Android vector 格式），`painterResource(Res.drawable.xxx)`
- RTL：padding 用 `start/end`，方向图标用 `Icons.AutoMirrored.*`

## 依赖注入（Koin，已就位）

依赖：`koin-core` + `koin-android`(androidMain) + `koin-compose`(commonMain，Composable 里 `koinInject<T>()`)。

入口已接好：Android 在 `PoiteApp.onCreate()` 里先 `initPostHog(this)` 再 `initKoin { androidContext(this@PoiteApp) }`；iOS 在 `iOSApp.swift` 的 `init()` 里 `IosKoinKt.startKoinIos()`（内含 PostHog 初始化）。`platformModule()` 是 expect/actual，放平台绑定（Android 的 Settings 需要 Context）。

**Kotlin 2.4 坑：给 Swift 的函数名不要以 `init` 开头**——K/N 为避免 ObjC initializer 冲突会改名加 `do` 前缀（`initKoinIos` → `doInitKoinIos()`），导致 Swift 侧旧调用编译失败。

新增 feature 时：

```kotlin
val homeModule = module {
    single<HomeRepository> { HomeRepositoryImpl(get(), get()) }
    factory { GetHomeDataUseCase(get()) }
    factory { HomeViewModel(get()) }
}
// 在 KoinInit.kt 的 modules(...) 里追加 homeModule
```

同类多实例用 `named()`；生产代码禁止 `stopKoin()`。

## 已就位的基础设施（core/）

- **AppLogger**（`core/logging`）：业务代码只依赖接口；实现是 Kermit（`KermitAppLogger`，platformLogWriter 自动接 logcat/os_log）。**不要**再手写 expect/actual 日志函数
- **KvStore**（`core/cache`）：multiplatform-settings 后端，Android 用 SharedPreferences、iOS 用 NSUserDefaults。只放配置/标志位
- **MemoryCache**（`core/cache`）：cache4k，进程内 10 分钟 TTL。接口响应等短生命周期对象
- **Tracker**（`core/tracking`）：**已接 PostHog**（posthog-kmp 0.11.0，需 Kotlin ≥2.4）——实现 `PostHogTracker`，配置集中在 `postHogConfig()`（projectToken 是公开客户端 key 可入库，debug=true 发布前关掉）。初始化在平台壳：Android `PoiteApp.onCreate` 调 `initPostHog(app)`（必须 Application，不能 Activity），iOS `startKoinIos()` 里调 `initPostHogIos()`。**事件规范（遵循 kmp-analytics skill）**：所有事件在 `AppEvent` sealed 类里唯一定义（name snake_case + params），禁止裸字符串；`track()` 在事件回调/ViewModel 里调，**不在 Composable 组合体里调**（重组双发）；曝光由 NavHost `DisposableEffect` 按路由统一发 `screen_view`（PostHog 自动 $screen 已关，避免 Android 双计）；接口含 setUserId/identify、setUserProperty、setSuperProperty（PostHog register）；测试用 commonTest 的 `FakeTracker` 断言事件名与参数。换埋点厂商只替换 Koin 绑定，业务代码零改动。iOS 链接走 Kotlin SwiftPM 集成（`integrateLinkagePackage`，见常用命令），生成 `iosApp/KotlinMultiplatformLinkedPackage/` 要提交，posthog-ios 不需单独加 SPM
- **CrashReporter**（`core/crash`）：**已接 Sentry**——实现 `SentryCrashReporter`，经 `io.sentry.kotlin.multiplatform.gradle` 插件（0.30.0）自动装 SDK；`initKoin` 里最早期调 `initializeSentry()`。iOS 侧 Sentry Cocoa 由 Xcode 工程 SPM 自行引入（provider=NONE + linker.xcodeprojPath 指向 iosApp.xcodeproj）。DSN 在 `core/crash/SentrySetup.kt`，是公开标识符可入库；生产环境的符号化（Android ProGuard mapping / iOS dSYM 上传）待 release 构建时配置
- 共同模式：**commonMain 接口 + Koin 绑定，三方 SDK 藏在实现层**——换厂商/换实现不动业务代码

## 装扮主题（feature/theme，已就位）

- **模型**：`ThemePack(id, name, version, previewColor, colors{light/dark}, assets{slot→url})`。色板只覆盖关键槽位（primary/onPrimary/background/surface/surfaceVariant/onSurface），其余 M3 默认推导；资源槽位见 `ThemeSlot`（avatar/homeIllustration/background/tabHome/tabSettings/splashPoster）
- **接口**：`GET api/theme/list`、`GET api/theme/{id}`（mock 同海报的本地 server 套路，adb reverse）
- **仓库**：`ThemePackRepository`——激活时下载素材到沙盒（files/theme/），`LoadedThemePack(pack, localAssets)` 持久化 KvStore，启动同步读取零网络；active=null 即默认主题（全内置）
- **换肤两个正交维度**：深浅色模式（ThemeController.mode）× 装扮色板（ThemePackRepository.active）。`AppTheme` 合成最终 colorScheme 并 provide `LocalThemeAssets`；图片统一 `ThemeAssetImage(slot, defaultRes)` 有素材用本地文件、无则回退内置图
- **启动海报**：激活时 `SplashRepository.setThemePoster()` 注入，priority=1（低于运营海报）
- **图片加载**：本地文件用 `decodeImageBitmap(path)`；网络预览图用 `NetworkImage`（内存 → 磁盘（cacheDir/images）→ 网络三级缓存，断网可读盘），字节解码 `decodeImageBytes`。别写无缓存的实时请求图片组件；列表大图场景换 Coil3
- **坑**：Compose 的 `Color(ULong)` 是打包位构造器，ARGB 必须用 `Color(Long)`（parseHexColor 已修正并有单测）

## 设置页 UI 规范（feature/settings，已就位）

- **分组**：`SettingsGroup`（小字组名 + 圆角卡片），组间 20dp；卡片内 `SettingsRow`（左标题 + 右侧值/控件）。新选项归入对应分组，禁止全页平铺
- **选项控件**：2~4 个短选项一律 `SegmentedSelector`（横向分段，占一行），禁止 Radio 纵向列表浪费空间；选项多或文案长才用列表行
- **标题**：工具型页面（设置等）省略大标题——`AppHeader()` 不传 title；品牌/内容页保留大标题
- 参考实现：`SettingsScreen`（外观分段 + 关于分组）

## 导航与 Header（已就位）

- **导航**：navigation-compose 2.9.2（KMP 官方），类型安全路由（`@Serializable data object XxxRoute`）。全局路由表在 `core/navigation/AppNavHost.kt`（根 Scaffold 含底部栏）；新页面 = 加 Route + `composable<XxxRoute>` 注册 + 页面自带 Scaffold
- **底部 Tab**：`AppBottomBar`——**悬浮胶囊样式**（圆角 50 浮起 + 阴影，选中项药丸高亮 surfaceVariant + primary 色）。新 tab 在 `tabs` 列表加一项 + 注册 composable。tab 切换用 `popUpTo(HomeRoute::class) saveState + launchSingleTop + restoreState`；**顶级 tab 页不带返回键**，下钻页（非 tab）才用 `onBack`
- **Header**：统一 `core/ui/AppHeader`——**大标题样式**（左圆形头像/下钻页返回键 + 右侧胶囊操作组 + headlineLarge 大标题）。页面结构固定为 `Scaffold(topBar = { AppHeader(...) }) { padding -> 内容消费 padding }`；胶囊操作只埋点不做事时也要接 track 事件

## Liquid Glass 磨砂（Haze，已就位）

- 库：`dev.chrisbanes.haze:haze + haze-materials` **1.6.10**（1.7.x 要求 compileSdk 37，本机 36 不可用）
- 原理：跨端 backdrop 模糊（Skia 实现，非苹果原生折射）。根 `AppNavHost` provide `LocalHazeState` + 内容侧 `hazeSource`；胶囊用 `Modifier.glass(shape)`（`core/ui/Glass.kt`，clip + hazeEffect ultraThin）。**胶囊 Surface 底色必须用半透明主题色**（`surfaceContainerHigh.copy(alpha = 0.6f)`），不能用 Transparent——ultraThin 在纯色背景上几乎不可见
- **内容必须延伸到胶囊下方**才有磨砂：根 NavHost 只消费顶部 padding，页面底部留 ~120dp 空间防遮挡
- 新加 compose resources 字符串后若报 `_collectCommonMainString0Resources` 等生成代码不同步错误：`rm -rf shared/build/generated/compose` 再构建（增量生成 bug）
- 现有页面：`feature/home`（计数 demo + 设置入口）、`feature/settings`（主题三档）

## UI 自动化标识（testTag，已全量覆盖）

- 规范遵循 `.agents/skills/quality-engineering-selector-stability`：命名 `<screen>-<element>-<role>` kebab-case；只加叶子可交互/可断言元素；**id 是公开契约，不改名**；contentDescription 不当 test id 用
- Android 开 resource-id 映射：在 **MainActivity** 根内容 `Modifier.semantics { testTagsAsResourceId = true }`。注意它是 **Android 专属扩展属性**（commonMain 用不了；要 import `androidx.compose.ui.semantics.testTagsAsResourceId`），ComposeView 上没有该属性（别踩）
- 现有 id：home-click-button / home-counter-text / home-illustration-image、header-avatar-image / header-add-button / header-search-button / header-back-button、bottombar-home-tab / bottombar-settings-tab、settings-theme-{system,light,dark}-option、settings-pack-<id>-card、settings-mock-switch、settings-mock-<scenario>-option、splash-poster-image、app-mock-banner
- 新 UI 元素必须同步加 id；iOS 的 testTag→accessibilityIdentifier 桥接未实测，写 iOS UI 测试前先验证

## 存储 Mock（StorageFaultController，已就位）

- 与网络 Mock 同套思路：**独立开关**（开启后才展示场景选项）+ KvStore 场景 + 实时生效 + debug 限定。场景：NORMAL / READ_FAIL / WRITE_FAIL / ENOSPC / CORRUPT_IMAGE
- 注入点：`StorageStats` 的读函数抛 IOException；各域 `save()` 抛 IOException（WRITE_FAIL/ENOSPC）；`ImageDiskCache.save` 在 CORRUPT_IMAGE 时写坏字节（解码失败回退默认图）
- **StorageManager 有异常路径**：usage() 失败返回 `error=true`（UI 显示「不可用」而非假 0）；clearCache() 返回 Boolean（失败弹「清除失败」）
- 脏数据场景（文件被外部删但元数据在）不需要专门 mock——手动删 files/theme 即可验证自愈
- 设置页开发者组有「存储异常」两行分段选择器；可与网络 Mock 组合（断网 + 存储异常）

## 存储管理（core/storage，已就位）

- **统一收口 `StorageManager`**：用量统计（递归目录大小）+ `clearCache()`。**分类按「能否再生成」而非目录**：缓存 = 图片磁盘缓存（cacheDir/images）、海报（files/splash）、主题素材（files/theme）；数据 = KvStore 文件（永不清）
- 各存储域必须暴露 `rootPath()` 并纳入 StorageManager 的 cacheDirs；新存储域照此登记
- **清理元数据自愈**：海报靠 activePoster 的文件存在过滤自动失效重下；主题调 `ThemePackRepository.onCacheCleared()` 回退默认显示但**保留 KV 里的包选择**，重新激活即重下；KvStore 一个字节不动
- 设置页「存储」组：缓存/数据用量 + 清除缓存（AlertDialog 确认），tag `settings-clear-cache-button`

## 网络 Mock（core/network/mock，已就位）

- **拦截层**：`installMockInterceptor()` 挂在统一 HttpClient 的 HttpSend 上（Ktor 3），命中场景构造 `HttpClientCall` 响应/抛异常/delay，否则放行。`HttpClientCall` 构造是 Ktor internal API（`io.ktor.utils.io.InternalAPI` 已 opt-in）
- **MockController**：开关 + 场景（NORMAL/ALL_500/ALL_TIMEOUT/OFFLINE/SLOW/EMPTY/BAD_JSON），KvStore 持久化、实时生效无需重启
- **仅 debug 可用**：`mockAvailable` expect/actual（Android FLAG_DEBUGGABLE，iOS Platform.isDebugBinary 需 ExperimentalNativeApi opt-in）
- **UI**：设置页「开发者」组（开关 + 两行分段场景）；active 时 App 根部顶部有 MOCK 标记条
- 空数据场景按 path 给空结构（list→[]、config→{"posters":[]}）；断网抛 IOException、超时抛 HttpRequestTimeoutException，与 safeApiCall 的 AppError 映射一一对应
- 细粒度 path 规则编辑器暂未做（需要时加 MockRule）

## 主题与夜间模式（core/theme，已就位）

- **入口**：`App()` 根部 `AppTheme { ... }`——按 `ThemeController` 的模式选 light/darkColorScheme。组件一律用 `MaterialTheme.colorScheme` 语义色，禁止写死颜色；根布局必须画 `colorScheme.background`（否则窗口底色不随主题）
- **三档**：SYSTEM / LIGHT / DARK，持久化在 KvStore（`theme_mode`）。系统切换自动重组；`ThemeController.isDark(systemDark)` 也给平台侧用
- **Android 系统栏**：MainActivity 里 `enableEdgeToEdge` + `SystemBarStyle.auto` 跟随主题（含强制模式），并 collect mode 流即时刷新
- **iOS 系统启动屏**：`LaunchBackground.colorset` 带 dark 外观（深色 #041619），`LaunchLogo.imageset` 有 dark 变体（无圆底的白 K + 橙三角）；App 内 Splash 页背景用主题色
- 设置页做主题开关时直接调 `ThemeController.setMode()`

## 网络（Ktor，已引入）

统一入口在 `core/network/`：**所有业务域共用 `createHttpClient()`**（Koin single 注入），禁止各自 new HttpClient。

- `ApiConfig.BASE_URL`：当前是 **Cloudflare Worker mock** `https://api.coolshell.net`（自有域名，poite-mock worker，素材 base64 内嵌单文件部署，wrangler 配置在 /tmp/poite_worker）。beeceptor 已弃用。本地联调替代方案：`adb reverse tcp:8931 tcp:8931` + 本地 python mock（iOS 真机访问不了 Mac localhost，必须用 Worker）
- 已装：ContentNegotiation（kotlinx JSON，ignoreUnknownKeys + isLenient）、HttpTimeout（10s）、`expectSuccess=true`（非 2xx 抛 ResponseException）
- 引擎：androidMain `cio`（**别用 okhttp**：Ktor 3.6 带的 okhttp-android 5.5 要求 compileSdk 37，本机只有 36）、iosMain `darwin`；commonTest 用 `ktor-client-mock`
- 各业务域 Api 只写相对 path（如 `SplashApiImpl` 的 `api/splash/config`），返回 domain 模型
- 结果统一 `Resource.Success/Error` + 类型化 `AppError`（Network/Http/Parse/Unknown），调用侧过 `safeApiCall {}`。**Ktor 3 的 JSON 解析失败抛 `JsonConvertException` 而非 SerializationException**，safeApiCall 已覆盖
- **接口成败只用 HTTP status 表达**（2xx = 成功），不设计业务 envelope 的 code 字段；错误细节进 AppError，不给 UI 裸 String
- 本地联调：`adb reverse tcp:8931 tcp:8931` 把设备 localhost 转发到 Mac mock 服务；debug 包经 `androidApp/src/debug/AndroidManifest.xml` 开了 cleartext（release 不带）
- `Auth bearer` 的刷新请求必须 `markAsRefreshTokenRequest()` 且 `sendWithoutRequest` 限定自己域名。证书绑定（androidMain OkHttp）必须主备双 pin

## 动态启动海报（feature/splash，已就位）

iOS/Android 系统启动屏都是静态的（bundle 签名只读），动态海报 = 系统启动屏 + App 内 Splash 页：

- **domain**：`SplashPoster(id, imageUrl, startAt, endAt, priority, localPath)`（epoch 毫秒，[start, end)）；`selectActivePoster()` 纯函数：已下载 + 生效中 → 优先级最高
- **data**：`SplashApi` 拉配置 → `SplashRepository.refresh()` 下载新图（URL 变化才重下）、清理失效文件、元数据 JSON 存 KvStore；`SplashFiles` expect/actual 管沙盒文件与 Bitmap 解码（iOS 用 POSIX stdio + Skia `toComposeImageBitmap()`，CMP 1.11 里不是 asImageBitmap）
- **presentation**：`SplashHost` 包在 `App()` 根部——主内容始终在底层组合，海报盖在上层；**主内容首帧就绪 && 至少 500ms 即切换**（防闪屏，无固定时长），点击立即跳过；无海报零延迟直进。**启动只读缓存不走网络**，`refreshAsync()` 在后台准备下次的
- 联调 mock 格式见 `SplashApiTest` 的 sampleJson

## 持久化（Room / DataStore KMP，需要时引入）

- Room KMP（KSP）：Database/DAO/Entity 放 commonMain，builder expect/actual（Android 用 Context，iOS 用 `NSHomeDirectory() + "/app.db"`）
- schema 升版本必须写 `Migration`；查询给 UI 的一律 `Flow<List<T>>`；多步写加 `@Transaction`
- 大数据集用 Paging 3（`PagingSource` + `Pager` + `.cachedIn(scope)`），禁止全量加载
- 需要 Room 时给 shared 加：KSP 插件、`androidx.room` 插件、`room-compiler` 用 `ksp { add("kspCommonMainMetadata", ...) }` 方式配置（Room KMP 特有，别按纯 Android 教程配）

## 测试

- 放 `commonTest`，`kotlin.test` + `runTest`；**用 Fake 不用 Mock**（不接 Mockito/MockK）
- KV 相关测试用 `multiplatform-settings-test` 的 `MapSettings`（1.3.0 起改名，旧名 MockSettings；已在 commonTest 依赖里）
- 验证：`./gradlew :shared:testDebugUnitTest`

## 日志

用 `AppLogger` 接口（Kermit 实现），注入使用；禁止 `println()`。任何端都不许记 token/邮箱/PII。崩溃上报接入后，release 的 WARN/ERROR 自动转 breadcrumb。

## 红线清单

1. **UI 一律写 commonMain Compose**；禁止给单端单独写原生页面（平台能力调用不算，走 expect/actual）
2. commonMain 禁止 `android.*`/`platform.*` import（uikitMain 之外的 expect/actual 例外）
3. 禁止 LiveData，统一 `StateFlow`/`Flow`
4. 禁止 `GlobalScope`；ViewModel 用 `SupervisorJob()` scope 并暴露 `onCleared()`
5. 一次性事件禁止放 UiState；事件 SharedFlow 禁止 `replay > 0`
6. `Resource.Error` 禁止裸 String，用类型化 `AppError`
7. 禁止把 `Context` 跨层传递——只在 platformModule 边界注入
8. 禁止硬编码用户可见字符串——走 compose resources
9. padding 禁止 `left/right`，用 `start/end`；方向图标用 `Icons.AutoMirrored.*`
10. **iOS Info.plist 必须含 `CADisableMinimumFrameDurationOnPhone=true` 和 `UILaunchScreen`**——删了会闪退/黑边
11. 版本号禁止写在 build.gradle.kts——一律进 `gradle/libs.versions.toml`
12. Room 升版本禁止不写 Migration；禁止无分页加载大数据集
13. 证书绑定必须主备双 pin
14. 有意义的 Image/Icon 必须有 `contentDescription`（无障碍）
15. 禁止 `println()` 日志；禁止记敏感数据
16. 横向能力（日志/埋点/崩溃/缓存）禁止直接依赖三方 SDK——必须经 `core/` 接口 + Koin 绑定；业务代码禁止出现 `co.touchlab.kermit` / `com.russhwolf.settings` 等 import

## 参考

- [KMP 官方文档](https://www.jetbrains.com/help/kotlin-multiplatform-dev/)
- [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/)
- [compose resources](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-resources-usage.html)
- [Android 架构指南](https://developer.android.com/topic/architecture)
- [Room KMP](https://developer.android.com/kotlin/multiplatform/room)
- [Koin Multiplatform](https://insert-koin.io/docs/reference/koin-mp/kmp)
- [SKIE](https://skie.touchlab.co)
