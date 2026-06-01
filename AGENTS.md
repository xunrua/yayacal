<!-- Generated: 2026-06-01 -->

# YaYa

纯 Android + Jetpack Compose 日历应用。功能：农历/节气/节日、个人班次排期（WORK/OFF 循环）、月/周/年三视图。

> 注意：README 中提及的 "Kotlin Multiplatform / iOS" 与当前代码不符。实际为纯 Android 项目，模块名为 `:core` 和 `:app`，无 `:shared` 或 `:androidApp`。

## 模块结构

| 模块 | 类型 | 职责 |
|------|------|------|
| `:core` | `com.android.library` | 所有 Compose UI、ViewModel、业务逻辑 |
| `:app` | `com.android.application` | 薄壳：MainActivity + Manifest + 主题 |
| `:macrobenchmark` | `com.android.test` | Baseline Profile 生成 |

**铁律**：`:app` 不添加业务逻辑，所有代码写在 `:core`。

## 常用命令

```bash
# 构建与安装
./gradlew :app:assembleDebug
./gradlew :app:installDebug

# 测试（仅 :core 有单元测试）
./gradlew :core:test
./gradlew :core:test --tests "plus.rua.project.ui.CalendarUtilsTest"

# 代码格式化
./gradlew spotlessApply

# Baseline Profile（需连接设备/模拟器）
./gradlew :macrobenchmark:updateBaselineProfile   # 生成 + 自动复制到 core/src/main/baseline-prof.txt
./gradlew :macrobenchmark:connectedBenchmarkAndroidTest

# 性能追踪（需连接设备）
./scripts/profile.sh          # 默认 8 秒
./scripts/profile.sh 15       # 自定义时长
./scripts/profile.sh --no-launch
```

输出：`logs/trace_*.perfetto-trace`、`logs/framestats_*.txt`、`logs/meminfo_*.txt`、`logs/report_*.md`。

## 构建配置要点

- **AGP** 9.2.1 · **Kotlin** 2.3.21 · **JVM target** 17
- **compileSdk/targetSdk** 37 · **minSdk** 24
- **版本目录**：`gradle/libs.versions.toml` — 所有依赖版本统一在此声明
- **构建类型**：`debug`（默认）、`release`（R8 混淆 + 资源压缩）、`trace`（release + trace 标记保留）、`benchmark`（macrobenchmark 专用）
- **R8**：`android.enableR8.fullMode=true`（`gradle.properties`）
- **缓存**：configuration cache + build cache 已启用（`gradle.properties`）
- **版本号**：动态生成格式 `baseVersion_gitHash_buildDate`（例：`1.0.0_a1b2c_010626`）

## 包名约定

- `:core` 逻辑层：`plus.rua.project`
- `:core` UI 层：`plus.rua.project.ui`
- `:app`：`plus.rua.project`

## 代码规范（关键差异项）

- 公共 `@Composable` 函数必须有 KDoc（参数说明 + 回调触发时机），见 `COMMENTS.md`
- `Modifier` 参数始终放签名最后
- 回调参数用 `on` 前缀（`onDateClick`）
- 可点击列表项用 `Card(onClick = ...)` + `CardDefaults.cardElevation(defaultElevation = 0.dp)`，不要用 `Modifier.clickable()` 裸包
- `@Suppress("DEPRECATION")` 必须附带行内注释说明原因（当前主要用于 `monthNumber`）
- UI 文本使用中文
- Spotless 在根 `build.gradle.kts` 配置，覆盖 `src/**/*.kt` 和 `*.gradle.kts`

## 关键依赖

- **Compose BOM** `2026.05.01` + **Material 3**
- **`kotlinx-datetime`** 0.8.0 — 所有日期逻辑（禁止 `java.util.Calendar`）
- **`tyme4kt`** 1.4.5 — 农历、节气、传统节日
- **`sketch`** 4.4.0 — GIF 显示（`sketch-compose` + `sketch-animated-webp`）

## 性能追踪标记

`ComposeTrace.kt` 提供自定义 Perfetto/Systrace 标记，关键标记名：
- `MonthView:Compose` / `YearView:Compose`
- `CalendarPager:Page:*` / `WeekPager:Page`
- `VM:collapseProgress:*`
- `YearGridView:*`

详见 `DEVELOPMENT.md`。

## 参考文档

| 文件 | 内容 |
|------|------|
| `CLAUDE.md` | 完整架构说明、组件树、动画机制、Pager 映射逻辑 |
| `DEVELOPMENT.md` | 性能追踪、Baseline Profile、模拟器启动参数 |
| `COMMENTS.md` | KDoc 规范、注释原则、反模式清单 |
| `app/AGENTS.md` | `:app` 模块细则 |
| `core/AGENTS.md` | `:core` 模块细则 |
| `macrobenchmark/AGENTS.md` | Baseline Profile 模块细则 |
