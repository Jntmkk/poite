# Android 发布

发布只到 GitHub Release，不上传应用商店，不包含应用内自动更新。

## 流程

1. 向 `master` 提 PR。PR 标题使用 [Conventional Commits](https://www.conventionalcommits.org/)，并通过 squash merge 保留该标题：
   - `feat: ...`：增加 minor 版本。
   - `fix: ...`：增加 patch 版本。
   - `feat!: ...` 或正文含 `BREAKING CHANGE:`：增加 major 版本。
   - `chore:`、`docs:`、`ci:` 等不会单独触发发布。
2. CI 运行共享模块测试及 Android Debug 构建。
3. 合入 `master` 后，Release Please 创建或更新 Release PR，包含 `CHANGELOG.md`、`version.txt` 和 Android 版本变更。
4. 检查 Release PR 后合并它，即确认发布。
5. Release Please 创建 `vX.Y.Z` Tag 和 GitHub Release。Release 正文来自变更 PR/提交，包含分类日志及 PR 链接。
6. 同一工作流检出该 Tag，构建签名 APK/AAB、校验 APK 签名，再上传到对应 Release。更新日志不会被构建步骤改写。

`versionCode = major * 1,000,000 + minor * 1,000 + patch`，因此 minor、patch 必须在 `0..999`。首个自动化版本从 `1.0.0` 开始；这使它的 Android `versionCode` 为 `1000000`。

## 首次配置

在仓库 **Settings → Secrets and variables → Actions** 添加以下 Repository secrets：

| Secret | 内容 |
| --- | --- |
| `POITE_KEYSTORE_BASE64` | 固定 Release Keystore 的 Base64 内容 |
| `POITE_KEYSTORE_PASSWORD` | Keystore 密码 |
| `POITE_KEY_ALIAS` | 密钥别名 |
| `POITE_KEY_PASSWORD` | 密钥密码 |

可使用 `base64 -i release.jks | pbcopy` 将 Keystore 内容复制到剪贴板。不要将 Keystore、密码或 `.env` 文件提交到仓库，并将 Keystore 离线备份。密钥更换后，已有安装无法作为同一应用正常覆盖升级。

同时在 **Settings → Actions → General → Workflow permissions** 允许 GitHub Actions 创建 Pull Request。将 `Test and build Android` 设为 `master` 的必需检查，并启用分支保护。上述仓库设置无法由工作流文件自行开启。

## 发布产物

- `poite-X.Y.Z.apk`：Android 直接安装包。
- `poite-X.Y.Z.aab`：Android App Bundle 归档。
- `checksums.txt`：APK/AAB 的 SHA-256。

构建从已创建的版本 Tag 检出，而不是使用可能继续变化的 `master`。发布 Job 在签名 Secrets 缺失、测试、构建或签名校验失败时停止，不上传未签名产物。此时 GitHub Release 可能已由上一步创建；补齐 Secrets 或修复后，可重新运行失败的构建 Job。

上传不会覆盖同名资产。若同一 Tag 重跑上传时已有资产，上传会冲突；不要在未确认的情况下删除已发布版本或资产。
