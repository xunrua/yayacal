# YaYa

基于 Kotlin Multiplatform 与 Compose Multiplatform 的跨平台日历应用,Android 与 iOS 共享同一套 UI 与业务逻辑。

## 特性

- **流畅的视图切换** —— 月视图、周视图、年视图三种模式,拖拽手势驱动月↔周折叠,弹簧动画自动吸附
- **无限滑动分页** —— 基于 `Int.MAX_VALUE` 的虚拟分页,前后无边界翻页
- **完整中式日历** —— 公历 + 农历 + 二十四节气 + 传统节日,ISO 8601 周起始(周一)
- **个人排班周期** —— 自定义工作/休息循环,与公共节假日独立
- **Material 3 设计** —— 动态配色,深色模式

## 技术栈

- Kotlin 2.3 · Compose Multiplatform 1.11 · Material 3
- `kotlinx-datetime` 处理所有日期逻辑
- `tyme4kt` 提供农历、节气与传统节日
- `sketch` 渲染 GIF 动画
- 双模块:`:shared`(UI + 逻辑) · `:androidApp`(薄壳)
- iOS 入口为 `MainViewController.kt`,Xcode 工程位于 `iosApp/`

线条小狗表情包来自 https://www.douban.com/group/topic/264788645/?_i=9181692phrDzjR,9241256phrDzjR

## 快速开始

```bash
# 编译 Android debug APK
./gradlew :app:assembleDebug

# 安装到设备
./gradlew :app:installDebug

# 编译 release APK（含 Baseline Profiles）
./gradlew :app:assembleRelease

./gradlew :app:installBenchmark
```

Baseline Profile 自动生成器。

运行方式（一键生成 + 自动复制到 :core）：

```
./gradlew :macrobenchmark:updateBaselineProfile
```

仅运行基准测试（不自动复制）：

```
./gradlew :macrobenchmark:connectedBenchmarkAndroidTest
```

手动复制路径：
`macrobenchmark/build/outputs/connected_android_test_additional_output/`

测试覆盖全部用户交互路径，实现全量 AOT：

1. 冷启动 → 首帧渲染
2. FAB 展开 → 年视图 → 月视图
3. 日期选择 → 周视图折叠/展开
4. 关于页 → 开源许可页
5. 返回主界面
