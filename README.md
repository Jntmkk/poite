# poite

Kotlin Multiplatform + Compose Multiplatform 应用，共享 Android/iOS 的 UI 与业务代码。

## 开发检查

```bash
./gradlew :shared:testDebugUnitTest :androidApp:assembleDebug
```

PR 与 `master` 推送会自动运行上述检查。发布流程见 [docs/releasing.md](docs/releasing.md)。
