# Changelog

All notable changes to the YaYa project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-05-20

### Added

#### Project Foundation
- Kotlin Multiplatform project setup targeting Android and iOS with Compose Multiplatform
- Two-module architecture: `:shared` (UI + business logic) and `:androidApp` (thin Android shell)
- AGP 9.2.1, Gradle 9.5.1, Kotlin 2.3.21, JVM target 17
- Compile SDK / target SDK 37, minimum SDK 24
- Version catalog at `gradle/libs.versions.toml`
- Dynamic version name generation: `baseVersion + git hash + buildDate`

#### Calendar Core Views
- **Month View**: 6x7 day grid with dynamic row count (4/5/6 weeks) based on actual month shape
- **Week View**: Single-week horizontal pager activated by collapsing the month view
- **Year View**: 4x3 mini-month grid with swipe-based year navigation and Hero Zoom transition
- **Infinite Paging**: `Int.MAX_VALUE` virtual pages centered at `Int.MAX_VALUE / 2`, enabling boundless month/week/year navigation
- ISO 8601 week numbering with Monday as the first day of the week
- Today indicator with border outline; selected-today state uses `primaryContainer` for softer visual treatment
- Non-current-month days are grayed out for visual clarity
- Auto-select today (if within range) or first day / Monday on page changes
- Click month header to jump back to today
- Cross-month week selection: intelligently selects the appropriate date based on swipe direction

#### Collapse / Expand Animation
- Drag-driven month-to-week collapse gesture on the calendar grid
- Spring-based snap animation that auto-settles to the nearest state on release
- Two-phase whole-block slide-up collapse animation with fade-out for non-selected rows
- Dynamic drag range computed from actual visual height change for 1:1 finger tracking
- Fling velocity threshold (800 dp/s) for quick swipe snap
- Collapse threshold set to 8% for easy fold/unfold triggering
- Pull-down gesture to expand from collapsed week view back to month view
- Week pager cross-fade transition to eliminate blank gaps during page switches

#### Chinese Calendar (Lunar)
- Lunar date display below each day number (using `tyme4kt`), showing month name on the first day of each lunar month
- Twenty-four solar terms (节气) annotations
- Traditional lunar festivals (春节, 端午, 中秋, etc.)
- Western solar festivals
- Legal holiday and compensatory workday badges (休/班) in the top-right corner
- Priority-based annotation display: legal holidays > lunar festivals > solar terms > solar festivals > lunar day

#### Personal Shift Scheduling
- `ShiftPattern` data model with anchor date + cyclic sequence for periodic work/rest schedules
- Independent from public holidays
- Work/Rest capsule badge display on DayCell
- Configurable legal holiday overlay toggle (default off)
- Shift status tips in the bottom card

#### Visual Design & Animations
- Material 3 dynamic color scheme
- System dark theme support with automatic light/dark `ColorScheme` switching
- DayCell circular reveal animation with `updateTransition` for smooth state transitions
- Month header slide + fade transition when month/week number changes
- Page fade-in/fade-out transitions on CalendarPager and WeekPager
- GIF elastic entrance animation on switch
- 152 GIF assets displayed randomly based on selected date seed
- Custom app icon resources for all densities and platforms (PNG + WebP)

#### Bottom Card
- Draggable bottom info card with drag handle
- Selected date relative day description (今天, 昨天, 明天, N天前/后)
- Gregorian and lunar date details
- Shift status tips (WORK / OFF)
- Random GIF display (140dp height)

#### Navigation & Pages
- About screen with app icon, name, dynamic version, and open-source license entry
- Licenses screen listing all third-party dependency licenses
- Floating Action Button (FAB) with zoom-animated menu and scrim close
- Page navigation with direction-aware slide + fade transitions
- Android 13+ Predictive Back gesture with follow-finger displacement/scale animation
- BackHandler expect/actual for cross-platform back gesture interception

#### Performance Optimizations
- `ComposeTrace` cross-platform trace markers for Perfetto / Systrace analysis
- `graphicsLayer(translationY)` replacing `offset(Dp)` to avoid per-frame layout passes
- SolarDay static cache to eliminate repeated object creation during pager switches
- MiniMonth pure Canvas rendering: eliminated 96 Text component measurement overhead
- Year view / month view coexistence in composition tree with `Modifier.alpha` control (avoiding whole-tree destruction)
- Precomputed dp-to-px conversions and TextLayoutResult caches
- Pager cache optimized (`beyondViewportPageCount = 0`)

#### Build & Testing
- R8 code shrinking and resource optimization with ProGuard rules
- ABI filtering (`arm64-v8a`, `armeabi-v7a`)
- App Bundle language/density/ABI splits
- Unit tests for `CalendarViewModel`, `CalendarUtils`, and `ShiftPattern`
- `kotlinx-coroutines-test` for coroutine-based ViewModel testing
- ComposeTrace host test fallback (silently ignores when Trace API is not stubbed)

#### Documentation
- `CLAUDE.md` with project architecture, conventions, and build commands
- `DEVELOPMENT.md` with setup, build, test, and Perfetto trace analysis guide
- `COMMENTS.md` with commenting and KDoc conventions
- `README.md` with feature overview and tech stack

### Changed

- Row padding increased from 4dp to 6dp for better touch targets
- Selected-state animation duration reduced from 250ms to 150ms for snappier feedback
- Weekday header moved out of pager to remain fixed across page swipes
- Card gap spacing animates with collapse progress (24dp expanded → 12dp collapsed)
- Year view title bar displays lunar干支+生肖 year (e.g., 「丙午马年」) with a "今年" button for quick return
- FAB fixed to bottom-left of screen instead of tracking BottomCard height
- Menu scrim changed to fully transparent without fade animation

### Fixed

- First-frame flicker by deferring row height until measured
- Calendar height jitter when collapsing to week view
- Swipe interpolation discontinuity during month transitions
- Collapse drag not tracking finger (now uses dynamic dragRange)
- BottomCard positioning during collapse animation
- Flash when expanding after navigating months in collapsed state
- Week number baseline alignment stability during AnimatedContent transitions
- Year view stale year display on enter
- Year view missing scale animation on first launch
- "Today" button title bar jitter: replaced conditional rendering with alpha fade
- Folded state cross-month date not grayed out in week view
- Theme switching transparency issues by adding explicit background colors
- Predictive back gesture failure and end-of-animation flash on certain devices
- Back animation residual transition eliminated with `snapTo`
- Icon colors now adapt to `MaterialTheme` instead of hardcoded white

### Removed

- MonthHeader click-to-toggle-year-view (replaced by FAB menu)
- Year view arrow navigation (replaced by swipe gesture)
- Shift badge background circle for lighter visual weight
- Default ripple effect on DayCell (replaced by circular reveal animation)
- Aliyun Maven mirrors (switched back to Maven Central / Google)
- Unused Compose runtime ProGuard keep rules

## [Unreleased]

- No unreleased changes at this time.

---

[1.0.0]: https://github.com/xfy/yayacal/releases/tag/v1.0.0
