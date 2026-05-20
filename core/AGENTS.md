<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-05-20 | Updated: 2026-05-20 -->

# core

## Purpose
Android Library 核心模块，包含所有 Jetpack Compose UI、ViewModel 和业务逻辑。作为 `:app` 模块的依赖库提供，是项目的主体代码所在。

## Key Files

| File | Description |
|------|-------------|
| `build.gradle.kts` | 核心模块构建配置（Android Library 插件、Compose 编译器、依赖） |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `src/main/` | 所有 Compose UI、ViewModel 和业务逻辑（见 `src/main/AGENTS.md`） |
| `src/test/` | 单元测试套件（见 `src/test/AGENTS.md`） |

## For AI Agents

### Working In This Directory
- 所有功能代码应放在 `src/main/` 中
- 包名：`plus.rua.project`（逻辑层）、`plus.rua.project.ui`（UI 层）
- 修改核心逻辑后需运行 `:core:test` 验证

### Testing Requirements
- 全部测试：`./gradlew :core:test`
- 单类测试：`./gradlew :core:test --tests "ClassName"`

### Common Patterns
- 公共 `@Composable` 函数需要 KDoc（见 `COMMENTS.md`）
- `Modifier` 参数始终放在签名最后
- 回调参数使用 `on` 前缀（`onDateClick`、`onMonthChanged`）
- UI 文本使用中文

## Dependencies

### External
- Jetpack Compose BOM, Material 3
- `kotlinx-datetime`, `kotlinx-coroutines`
- `tyme4kt`（农历/节气/节日）
- `sketch`（GIF 显示）
- `androidx-lifecycle-viewmodel-compose`

<!-- MANUAL: -->
