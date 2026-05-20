# YaYa 功能路线图

> 由创意 brainstorm 整理，按优先级和实现难度排序。
> 状态: `[-]` 待办 / `[~]` 进行中 / `[x]` 已完成

---

## P0 — 快速落地（1-2 天，零外部依赖）

| 状态 | 功能 | 方向 | 关键文件 | 备注 |
|------|------|------|---------|------|
| `[-]` | 呼吸今日 | 视觉 | `DayCell.kt` | 今天日期圆圈 3 秒呼吸缩放动画 |
| `[-]` | 涟漪选日 | 视觉 | `DayCell.kt` | 点击扩散涟漪，颜色随季节变化 |
| `[-]` | 节气物语 | 文化 | `BottomCard.kt` | 节气当天展示物候/习俗/饮食建议 |
| `[-]` | 诗词日签 | 文化 | `BottomCard.kt` | 每日一句诗词，打字机效果 |
| `[-]` | 月相盈亏 / 月相微标 | 文化+视觉 | `DayCell.kt`, `BottomCard.kt` | 朔望月算法 + Canvas 绘制 |
| `[-]` | 干支纪年 | 文化 | `MonthHeader.kt` | 标题区展示干支+生肖 |

---

## P1 — 短期实现（1 周内）

| 状态 | 功能 | 方向 | 关键文件 | 备注 |
|------|------|------|---------|------|
| `[-]` | 工时年报 | 数据 | 新建 `YearlyReportEngine.kt` | 年度轮班统计报告，Canvas 分享图 |
| `[-]` | 热力年历 | 数据 | `YearGridView.kt` | GitHub contribution 风格全年热力图 |
| `[-]` | 灵犀速记 | AI | 新建 `NlpParser.kt` | 中文正则 NLP，语音转日程 |
| `[-]` | 花信风 | 文化 | `DayCell.kt`, `BottomCard.kt` | 二十四番花信风 + 诗词 |
| `[-]` | 今日宜忌 | 文化 | `BottomCard.kt` | 干支十二建除趣味宜忌 |
| `[-]` | 节日溯源 | 文化 | `BottomCard.kt` | 传统节日起源故事可折叠卡片 |
| `[-]` | 节气焕色 | 视觉 | `CalendarMonthView.kt` | 主题色随 24 节气渐变 |
| `[-]` | 农事谚语 | 文化 | `BottomCard.kt` | 按节气/月份展示谚语 |
| `[-]` | 四季主题 | 视觉 | `CalendarMonthView.kt` | 季节自动切换主题色 |

---

## P2 — 中期实现（2-4 周）

| 状态 | 功能 | 方向 | 关键文件 | 备注 |
|------|------|------|---------|------|
| `[x]` | 智能排班 | AI | `ShiftPattern.kt` | overrides + 滑动窗口学习 |
| `[x]` | 日历明信片 | 数据+视觉 | `PostcardExporter.kt` | Compose → Bitmap，多模板分享 |
| `[x]` | 假勤博弈 | 数据 | `LegalHolidayAnalyzer.kt` | 轮班 vs 法定假日对比 |
| `[x]` | streak 挑战 | 数据 | `HabitTracker.kt` | 习惯打卡 + 火焰动画 |
| `[x]` | 周期先知 | 数据 | `CyclePredictor.kt` | 历史标记周期预测 |
| `[x]` | 时间饼图 | 数据 | `TimePieChart.kt` | Canvas 饼图 + 下钻 |
| `[x]` | 节奏雷达 | 数据 | `RhythmRadar.kt` | 五维雷达图 |
| `[x]` | 星夜背景 | 视觉 | `StarryNightBackground.kt` | 暗色模式星空粒子 |
| `[x]` | 触感翻页 | 视觉 | `HapticFeedback.kt` | Android HapticFeedbackConstants |
| `[x]` | 时辰能量 | 文化 | `ShichenEnergy.kt` | 十二时辰 LazyRow + 五行属性 |
| `[x]` | 3D 翻页 | 视觉 | `FlipPageEffect.kt` | `rotationY` 翻页效果 |
| `[x]` | 动态图标 | 视觉 | `DynamicShortcutManager.kt` | Android ShortcutManager 动态更新 |

---

## P3 — 长期规划（需要外部依赖或较大架构调整）

| 状态 | 功能 | 方向 | 关键文件 | 备注 |
|------|------|------|---------|------|
| `[-]` | 天气织历 | AI+视觉 | `CalendarMonthView.kt` | Open-Meteo API + 背景色调 |
| `[-]` | 生日星图 | AI | 平台代码 | 联系人权限 + 农历生日提醒 |
| `[-]` | 时光胶囊 | AI | 新建 `TimeCapsule.kt` | 加密存储 + 到期解锁 |
| `[-]` | 待办浮窗 | AI | `BottomCard.kt` | 三段式手势 + SQLDelight |
| `[-]` | 日程分身 | AI | `CalendarViewModel.kt` | 多层日历 + 自动归类 |
| `[-]` | 毛玻璃年视 | 视觉 | `YearGridView.kt` | Compose Multiplatform blur 有限 |

---

## 数据统计

- 总计: **32** 个功能创意
- P0 (快速落地): **6** 个
- P1 (短期): **9** 个
- P2 (中期): **12** 个
- P3 (长期): **6** 个

---

## 依赖清单

| 功能组 | 可能需要的新依赖 |
|--------|-----------------|
| 语音输入 | Android `SpeechRecognizer`, iOS `SFSpeechRecognizer` (平台 API，无需库) |
| 数据持久化 | SQLDelight 或 DataStore/NSUserDefaults (expect/actual) |
| 天气 | Open-Meteo API (Ktor HTTP，无需 API Key) |
| 加密 | krypt 或平台 expect/actual 加密 |
| 图表 | 纯 Compose Canvas (无需第三方图表库) |

---

*最后更新: 2026-05-21*
