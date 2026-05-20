<!-- Parent: ../../AGENTS.md -->
<!-- Generated: 2026-05-20 | Updated: 2026-05-20 -->

# test

## Purpose
核心模块的单元测试 source set，包含 ViewModel、班次模型和日历工具函数的测试。

## Key Files

无（目录仅包含子目录）

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `kotlin/plus/rua/project/` | 核心逻辑测试（见 `kotlin/plus/rua/project/AGENTS.md`） |
| `kotlin/plus/rua/project/ui/` | UI 工具函数测试（见 `kotlin/plus/rua/project/ui/AGENTS.md`） |

## For AI Agents

### Working In This Directory
- 测试使用 `kotlin.test` 和 `kotlinx-coroutines-test`
- 新增测试后运行 `./gradlew :core:test`
- 测试类名遵循 `*Test.kt` 约定

### Dependencies

#### External
- `kotlin-test-junit`
- `kotlinx-coroutines-test`

<!-- MANUAL: -->
