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
4. 选择发布方式：
   - **常规发布**：检查并手动合并 Release PR。
   - **星号自动发布（默认）**：源分支名包含 `*`，例如 `feat/new-screen-*`。该 PR 在 CI 通过后自动 squash 合并到 `master`，随后自动维护并合并 Release PR，继续构建与发布，无需人工二次确认。
   - **显式自动发布**：也可在 PR 标题、正文或分支名写 `[release]`；用 `[release=minor]`、`[release=major]` 指定升级幅度。PR 标题/合并提交中的 `feat*:`、`fix*:` 等星号形式也会触发发布。
5. 若自动发布时已有待发布 Release PR，沿用该 PR 计算出的版本；若尚未有 Release PR，则自动创建一个。没有 Release PR 且提交不符合常规版本规则时，星号或 `[release]` 默认增加 patch；`feat:` / `feat!:` 等常规提交优先按自身语义升级。
6. Release Please 创建 `vX.Y.Z` Tag 和**草稿** GitHub Release。Release 正文来自变更 PR/提交，包含分类日志及 PR 链接。
7. 工作流检出该 Tag，构建签名 APK/AAB、校验 APK 签名与版本，上传资产后才公开 Release。更新日志不会被构建步骤改写。

示例：`feat/new-screen-*`、`fix: 修复缓存 [release]`、`fix*: 修复缓存`、`[release=minor]`。自动发布依赖功能 PR 已通过 CI。仓库需开启 **Allow auto-merge**；若 `master` 要求 PR 审查，机器人自动合并仍需满足该规则。

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

构建从已创建的版本 Tag 检出，而不是使用可能继续变化的 `master`。发布 Job 在签名 Secrets 缺失、测试、构建或签名校验失败时停止，不公开 Release 或上传未签名产物。此时可能存在不可见的草稿 Release 和已创建的 Tag；补齐 Secrets 或修复构建问题后，可在 Actions 中重新运行失败的构建 Job，完成后再公开该草稿。

上传不会覆盖同名资产。若同一 Tag 重跑上传时已有资产，上传会冲突；不要在未确认的情况下删除已发布版本或资产。
