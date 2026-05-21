# Baseline Profiles 保留规则：确保方法名不被 R8 混淆，使 profile 规则匹配正确
-keepattributes SourceFile,LineNumberTable

# ========== 启动热点路径保留 ==========

# DayCell — 启动最热点
-keepclassmembers class plus.rua.project.ui.DayCellKt {
    public static void DayCell(...);
}

# LunarCache — 日期计算缓存
-keepclassmembers class plus.rua.project.LunarCache {
    public static plus.rua.project.DayCellInfo getOrCompute(kotlinx.datetime.LocalDate);
    public static java.lang.String formatLunarDate(kotlinx.datetime.LocalDate);
    public static void precompute(...);
}

# DayCellInfo 数据类
-keepclassmembers class plus.rua.project.DayCellInfo {
    public java.lang.String getAnnotationText();
    public boolean getIsAnnotationHighlight();
    public java.lang.String getHolidayBadge();
    public java.lang.String getLunarMonthName();
}

# CalendarMonthPage
-keepclassmembers class plus.rua.project.ui.CalendarMonthPageKt {
    public static void CalendarMonthPage(...);
    public static java.util.List generateMonthDays(...);
}

# CalendarMonthView
-keepclassmembers class plus.rua.project.ui.CalendarMonthViewKt {
    public static void CalendarMonthView(...);
}

# CalendarPager
-keepclassmembers class plus.rua.project.ui.CalendarPagerKt {
    public static void CalendarPager(...);
}

# WeekPager
-keepclassmembers class plus.rua.project.ui.WeekPagerKt {
    public static void WeekPager(...);
}

# BottomCard
-keepclassmembers class plus.rua.project.ui.BottomCardKt {
    public static void BottomCard(...);
}

# CalendarViewModel
-keepclassmembers class plus.rua.project.CalendarViewModel {
    public <init>(...);
    public kotlinx.datetime.LocalDate getSelectedDate();
    public java.util.List getMonthDays(...);
}

# ========== 全量业务类保留 ==========
# 保留所有业务类名和方法名，确保 profile 通配规则始终匹配

# CalendarUtils 所有方法
-keepclassmembers class plus.rua.project.ui.CalendarUtilsKt {
    public static *;
}

# MonthHeader
-keepclassmembers class plus.rua.project.ui.MonthHeaderKt {
    public static *;
}

# WeekdayHeader
-keepclassmembers class plus.rua.project.ui.WeekdayHeaderKt {
    public static *;
}

# YearGridView / YearHeader / MiniMonth
-keepclassmembers class plus.rua.project.ui.YearGridViewKt {
    public static *;
}

# AboutScreen
-keepclassmembers class plus.rua.project.ui.AboutScreenKt {
    public static *;
}

# LicensesScreen / LicenseItem
-keepclassmembers class plus.rua.project.ui.LicensesScreenKt {
    public static *;
}
-keepclassmembers class plus.rua.project.ui.LicensesKt {
    public static *;
}

# AnimatedGif
-keepclassmembers class plus.rua.project.ui.AnimatedGifKt {
    public static *;
}

# CalendarViewModel$CalendarDay
-keepclassmembers class plus.rua.project.CalendarViewModel$CalendarDay {
    public *;
}

# ShiftPattern
-keepclassmembers class plus.rua.project.ShiftPattern {
    public *;
}

# AppInfo
-keepclassmembers class plus.rua.project.AppInfo {
    public static *;
}

# ComposeTrace
-keepclassmembers class plus.rua.project.ComposeTraceKt {
    public static *;
}

# Platform
-keepclassmembers class plus.rua.project.PlatformKt {
    public static *;
}
