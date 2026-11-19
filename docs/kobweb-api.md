# Шпаргалка по API Kobweb / compose-html в этом проекте

Собрана по исходникам версий, которые реально подключены в проекте. **Читать файл здесь, а не
распаковывать `-sources.jar` / klib из `~/.gradle/caches`** — это экономит десятки вызовов инструментов.

Версии (`gradle/libs.versions.toml`):

| Библиотека                                                    | Версия |
|---------------------------------------------------------------|--------|
| `com.varabyte.kobweb` (core, compose, silk, kobwebx-markdown) | 0.25.1 |
| `org.jetbrains.compose.html:html-core`                        | 1.11.1 |
| `androidx.compose.runtime`                                    | 1.12.0 |
| Kotlin                                                        | 2.4.10 |

Проект frontend-only (`jsMain`), сервера нет. Запуск: `cd site && kobweb run`, экспорт:
`kobweb export --layout static`.

---

## 1. Modifier → HTML

- Тип: `com.varabyte.kobweb.compose.ui.Modifier`. Есть `infix fun then(other: Modifier)`.
- В `attrs` HTML-компоновщиков превращается через `Modifier.toAttrs { attr(...) }`
  (`com.varabyte.kobweb.compose.ui.toAttrs`).
- `Modifier.styleModifier { /* this: StyleScope */ }` (`com.varabyte.kobweb.compose.ui.styleModifier`) —
  **главный escape hatch**: внутри доступен `StyleScope`, то есть любое CSS-свойство через
  `property("name", value)` / `property("name", value, important = true)`, даже если для него нет
  типизированного модификатора.
- Silk-стили: `CssStyle.toModifier()` и `CssStyle.toAttrs()` из `com.varabyte.kobweb.silk.style`.

### CssStyle

```kotlin
val SomeStyle = CssStyle.base { Modifier.fillMaxWidth() }

val OtherStyle = CssStyle {
    base { Modifier.fillMaxSize().padding(leftRight = 2.cssRem) }
    cssRule("h1") { Modifier.fontSize(3.cssRem) }          // вложенные селекторы (потомки)
    cssRule(" :is(li,ol,ul)") { Modifier.margin(bottom = 0.25.cssRem) }
    Breakpoint.MD { Modifier.maxWidth(60.cssRem) }          // медиазапрос по брейкпоинту
}
```

Глобальные правила: в `@InitSilk` — `ctx.stylesheet.registerStyle("html") { ... }`,
`registerStyleBase`, `ctx.theme.modifyStyleBase(SomeStyle) { ... }`,
`cssRule(CSSMediaQuery.MediaFeature("prefers-reduced-motion", StylePropertyValue("no-preference"))) { ... }`.

### Брейкпоинты

`com.varabyte.kobweb.silk.style.breakpoint.Breakpoint` (`SM`, `MD`, `LG`, `XL`), внутри `CssStyle { }`
используется как блок. Готовые модификаторы: `displayIfAtLeast(Breakpoint.MD)`,
`displayUntil(Breakpoint.MD)` из `com.varabyte.kobweb.silk.style.breakpoint`.

---

## 2. Единицы и цвета

- Единицы: `px`, `percent`, `cssRem`, `em`, `ms`, `fr`, `vh`, `vw` — `org.jetbrains.compose.web.css.*`.
  ⚠️ **`svh` / `dvh` в compose-html 1.11.1 нет** — только `vh`. Нужен `svh` →
  `styleModifier { property("max-height", "min(550px, 80svh)") }`.
- Нет типизированного значения `auto` для margin (в `CSSKeywords`/`CSSUnits` `AutoValue` не найден).
  Центрировать блок по горизонтали — через flex-обёртку (`display(DisplayStyle.Flex)` +
  `justifyContent(JustifyContent.Center)`) или `styleModifier { property("margin-inline", "auto") }`.
- Цвета: `com.varabyte.kobweb.compose.ui.graphics.Colors` (`Black`, `White`, `Transparent`,
  `LightGray`, `X.inverted()`), `Color.rgb(0xRRGGBB)`, `rgba(...)`.
- Палитра сайта: `ColorMode.current.toSitePalette()` / `colorMode.toPalette().color`
  (`SiteTheme.kt`), CSS-переменные — `Modifier.setVariable(ButtonVars.FontSize, 1.em)`.
- CSS-функции: `com.varabyte.kobweb.compose.css.functions.clamp(min, preferred, max)` — см. `NavHeader.kt`.

---

## 3. Модификаторы (проверенные сигнатуры)

Все — из `com.varabyte.kobweb.compose.ui.modifiers.*` (достаточно wildcard-импорта).

**SizeModifiers.kt**

```kotlin
fillMaxWidth(percent: CSSLengthOrPercentageNumericValue = 100.percent)
fillMaxHeight(percent: CSSPercentageNumericValue = 100.percent)
width(size) / height(size)
minWidth(size) / minHeight(size)
maxWidth(size) / maxHeight(size)
size(size) / size(width, height) / sizeIn(minWidth, minHeight, maxWidth, maxHeight)
```

**LayoutModifiers.kt**

```kotlin
aspectRatio(ratio: Number)
aspectRatio(width: Number, height: Number)   // напр. aspectRatio(16, 9)
aspectRatio(ratio: AspectRatio)
lineHeight(value: Number) / lineHeight(value: CSSLengthOrPercentageNumericValue)
margin(all) / margin(scope: MarginScope.() -> Unit)
margin(topBottom, leftRight)
margin(top, leftRight, bottom)
margin(top, right, bottom, left)              // все параметры со значением по умолчанию 0.px
marginInline(both) / marginInline(start, end) / marginInline(scope)
marginBlock(both) / marginBlock(start, end) / marginBlock(scope)
padding(аналогично margin, есть PaddingScope / PaddingInlineScope / PaddingBlockScope)
verticalAlign(verticalAlign: VerticalAlign) / verticalAlign(value: CSSLengthOrPercentageNumericValue)
clear(Clear) / resize(Resize) / zIndex(Int)
```

**BorderModifiers.kt**

```kotlin
border(...) / border(scope: BorderScope.() -> Unit)     // у BorderScope: color/style/width, width(top,right,bottom,left)
borderCollapse(BorderCollapse)
borderTop(width: CSSLengthNumericValue?, style: LineStyle?, color: CSSColorValue?)   // то же для Bottom/Left/Right
borderTop(scope: BorderSideScope.() -> Unit)            // BorderSideScope: color / style / width
borderRadius(12.px) / borderRadius(topLeft = 2.cssRem)
```

**OverflowModifiers.kt**

```kotlin
overflow(Overflow)                       // Overflow.Auto / Hidden / Scroll / Visible
overflow(overflowX, overflowY)
overflow { x(Overflow.Auto); y(Overflow.Hidden) }   // OverflowScope
overflowBlock / overflowInline / overflowWrap(OverflowWrap.BreakWord)
```

**MediaModifiers.kt**

```kotlin
objectFit(ObjectFit)                     // ObjectFit из com.varabyte.kobweb.compose.css (Contain / Cover / ...)
objectPosition(ObjectPosition) / objectPosition(CSSPosition)
mixBlendMode(MixBlendMode)
```

**AlignmentModifiers.kt** — двойные перегрузки: из compose-html (`Jb*`) и из `com.varabyte.kobweb.compose.css`.

```kotlin
justifyContent(JustifyContent)  // com.varabyte.kobweb.compose.css.JustifyContent.Center / Start / End / FlexStart / ...
alignItems(AlignItems) / alignContent(AlignContent) / alignSelf(AlignSelf)
justifyItems / justifySelf / placeContent(AlignContent, JustifyContent)
```

⚠️ `JustifyContent`, `AlignItems`, `TextAlign`, `ObjectFit`, `Overflow` есть и в
`org.jetbrains.compose.web.css`, и в `com.varabyte.kobweb.compose.css`. При wildcard-импортах
обоих пакетов — конфликт имён; в репозитории принято импортировать конкретный класс явно
(см. `AppStyles.kt`: `import com.varabyte.kobweb.compose.css.TextAlign`).

**DisplayModifiers.kt**: `display(DisplayStyle)` — `org.jetbrains.compose.web.css.DisplayStyle.Flex/Block/None/...`

**TextModifiers.kt**: `textAlign(TextAlign)` — в репозитории используется
`com.varabyte.kobweb.compose.css.TextAlign.Start`.

Прочие файлы модификаторов (не разбирались детально, но существуют): `Background`, `Box`, `Column`,
`Contain`, `Effects`, `Flex`, `Font`, `Grid`, `Image`, `List`, `Outline`, `Position`, `Scroll`,
`Shape`, `Stacking`, `Transform`, `Transition`, `Typography`, `Break`, `Accessibility`.

---

## 4. DOM-хелперы и контент

- `Div/Row/Column/Box/Spacer/SpanText/Source/Video/Svg/...`; базовые HTML-теги — `org.jetbrains.compose.web.dom.*`.
- `Row/Column/Box` из `com.varabyte.kobweb.compose.foundation.layout` принимают `Modifier` и
  `verticalAlignment/horizontalAlignment = Alignment.CenterVertically` и т.п.
- `BasePath.prependTo(path)` (`com.varabyte.kobweb.navigation.BasePath`) — префикс пути для статики
  (GitHub Pages и т.п.). Используется для `src` картинок/видео.
- Иконки Silk: `MoonIcon`, `SunIcon`, `HamburgerIcon`, `CloseIcon`, `Tooltip`, `Overlay`, `PopupPlacement`.

---

## 5. Markdown-страницы (kobwebx-markdown)

- Markdown берётся из `site/src/jsMain/resources/markdown/**`, каждая страница использует layout,
  указанный в файле; для статей это `components/layouts/MarkdownLayout.kt` (обёртка `Div(MarkdownStyle.toAttrs())`),
  который сам надстраивается над `PageLayout`.
- **Элементы Markdown рендерятся «голым» HTML без стилей**: в частности таблицы →
  `<table><thead><tbody><tr><th><td>` без границ, отступов и выравнивания.
  Оформление добавляется `cssRule("table")`, `cssRule("th, td")` и т.д. внутри `MarkdownStyle`
  (по вложенному селектору — самый дешёвый способ; альтернатива — подмена рендера через
  `MarkdownHandlers` в `@InitSilk`, но она требует кода на каждый тег).
- Вызов виджетов из Markdown: `{{{ .components.widgets.VideoPlayer("/videos/file.mp4") }}}`
  (путь относительно `resources/public/`, т.е. `/videos/x.mp4` → `resources/public/videos/x.mp4`).
- Frontmatter обязателен: `title` (проверяется в `@InitRoute` в `MarkdownLayout.kt`),
  плюс `summary`, `date` — используются генератором каталога в `site/build.gradle.kts`.
- `PageContentStyle` ограничивает ширину текста: `Breakpoint.MD { maxWidth(60.cssRem) }`, на мобиле —
  `padding(leftRight = 2.cssRem, top = 4.cssRem)`. Широкие блоки (таблицы, `pre`) внутри него
  выходят за экран → нужен `overflow-x: auto` на самом широком элементе, а не на странице.

---

## 6. Что проверять компиляцией, а не распаковкой jar

Имена членов enum'ов в `com.varabyte.kobweb.compose.css` (`VerticalAlign.*`, `BorderCollapse.*`,
`ObjectFit.*`) и точный набор значений `Overflow`/`TextAlign` в этом пакете по шпаргалке выше не
подтверждены построчно. Если компилятор ругается — пробуй соседнее имя или уходи в
`styleModifier { property("vertical-align", "middle") }`; это заведомо работает.
