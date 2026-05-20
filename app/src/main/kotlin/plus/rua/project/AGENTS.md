<!-- Parent: ../../../../../../AGENTS.md -->
<!-- Generated: 2026-05-20 | Updated: 2026-05-20 -->

# project

## Purpose
Android 应用入口源码目录，包含 `MainActivity.kt`、`AboutActivity.kt` 和 `LicensesActivity.kt`。

## Key Files

| File | Description |
|------|-------------|
| `MainActivity.kt` | Android 入口 Activity，继承 `ComponentActivity`，调用 `setContent { CalendarMonthView() }` |
| `AboutActivity.kt` | 关于页面 Activity，展示应用信息 |
| `LicensesActivity.kt` | 许可证列表 Activity，展示开源依赖许可 |

## Subdirectories
无

## For AI Agents

### Working In This Directory
- Activities 应保持极简，不要添加额外业务逻辑
- 所有 UI 和业务逻辑在 `:core` 模块中

## Dependencies

### Internal
- `:core` 模块 — `MainActivity` 调用 `CalendarMonthView()` Composable 入口

<!-- MANUAL: -->
