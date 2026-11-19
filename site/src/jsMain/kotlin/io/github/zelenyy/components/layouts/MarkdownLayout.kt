package io.github.zelenyy.components.layouts

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.OverflowWrap
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.core.data.add
import com.varabyte.kobweb.core.init.InitRoute
import com.varabyte.kobweb.core.init.InitRouteContext
import com.varabyte.kobweb.core.layout.Layout
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.toAttrs
import com.varabyte.kobweb.silk.theme.colors.palette.color
import com.varabyte.kobweb.silk.theme.colors.palette.toPalette
import com.varabyte.kobwebx.markdown.markdown
import io.github.zelenyy.toSitePalette
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div

val MarkdownStyle = CssStyle {
    base { Modifier.fillMaxSize() }

    cssRule("h1") {
        Modifier
            .fontSize(3.cssRem)
            .fontWeight(400)
            .margin(bottom = 2.5.cssRem)
            .lineHeight(1.2) //1.5x doesn't look as good on very large text
    }

    cssRule("h2") {
        Modifier
            .fontSize(3.cssRem)
            .fontWeight(300)
            .margin(topBottom = 2.cssRem)
    }

    cssRule("h3") {
        Modifier
            .fontSize(2.4.cssRem)
            .fontWeight(300)
            .margin(topBottom = 1.5.cssRem)
    }

    cssRule("h4") {
        Modifier
            .fontSize(1.2.cssRem)
            .fontWeight(FontWeight.Bolder)
            .margin(top = 1.cssRem, bottom = 0.5.cssRem)
    }

    cssRule("ul") {
        Modifier.fillMaxWidth().overflowWrap(OverflowWrap.BreakWord)
    }

    cssRule(" :is(li,ol,ul)") {
        Modifier.margin(bottom = 0.25.cssRem)
    }

    cssRule("code") {
        Modifier
            .color(colorMode.toPalette().color.toRgb().copyf(alpha = 0.8f))
            .fontWeight(FontWeight.Bolder)
    }

    cssRule("pre") {
        Modifier
            .margin(top = 0.5.cssRem, bottom = 2.cssRem)
            .fillMaxWidth()
    }
    cssRule("pre > code") {
        Modifier
            .display(DisplayStyle.Block)
            .fillMaxWidth()
            .backgroundColor(colorMode.toSitePalette().nearBackground)
            .border(1.px, LineStyle.Solid, colorMode.toPalette().color)
            .borderRadius(0.25.cssRem)
            .padding(0.5.cssRem)
            .fontSize(1.cssRem)
            .overflow { x(Overflow.Auto) }
    }

    // --- Цитаты ---
    // Браузер по умолчанию даёт blockquote только отступ слева и курсив, поэтому цитата визуально
    // почти не отличается от обычного текста. Добавляем акцентную полосу, подложку и «приглушённый» текст.
    cssRule("blockquote") {
        Modifier
            .fillMaxWidth()
            .margin(topBottom = 1.5.cssRem, leftRight = 0.px)
            .padding(topBottom = 0.75.cssRem, leftRight = 1.cssRem)
            .borderLeft(width = 3.px, style = LineStyle.Solid, color = colorMode.toSitePalette().brand.primary)
            .backgroundColor(colorMode.toSitePalette().nearBackground)
            .borderRadius(topRight = 0.25.cssRem, bottomRight = 0.25.cssRem)
            .color(colorMode.toPalette().color.toRgb().copyf(alpha = 0.85f))
            .styleModifier {
                property("font-style", "normal") // курсив в цитатах только мешает читать
            }
    }

    // Отступы абзацев внутри цитаты убираем у первого и последнего: между ними расстояние остаётся,
    // а лишняя «воздушная» подушка сверху и снизу цитаты — нет.
    cssRule("blockquote > p:first-child") {
        Modifier.margin(top = 0.px)
    }
    cssRule("blockquote > p:last-child") {
        Modifier.margin(bottom = 0.px)
    }

    // Вложенные цитаты не должны выглядеть как одна слипшаяся полоса
    cssRule("blockquote blockquote") {
        Modifier.margin(topBottom = 0.75.cssRem)
    }

    // --- Таблицы ---
    // Kobweb рендерит markdown-таблицы «голым» HTML без стилей: всё центрируется, строки сливаются,
    // а многострочные ячейки уезжают в соседнюю строку. Ниже — разделители строк, выравнивание и
    // горизонтальная прокрутка для узких экранов.
    cssRule("table") {
        Modifier
            .fillMaxWidth()
            // Таблица сама скроллится по горизонтали: иначе на смартфоне широкая таблица растягивает всю страницу
            .display(DisplayStyle.Block)
            .overflow { x(Overflow.Auto) }
            .margin(topBottom = 1.5.cssRem)
            .fontSize(0.95.cssRem)
            .lineHeight(1.4)
            .styleModifier {
                property("border-collapse", "collapse")
            }
    }

    cssRule("th, td") {
        Modifier
            .textAlign(TextAlign.Start) // левый край: текст не «прилипает» к строке соседа
            .padding(topBottom = 0.6.cssRem, leftRight = 0.9.cssRem)
            .borderBottom(
                width = 1.px,
                style = LineStyle.Solid,
                color = colorMode.toPalette().color.toRgb().copyf(alpha = 0.25f)
            )
            .styleModifier {
                property("vertical-align", "middle") // текст в ячейке по высоте строки по центру
            }
    }

    cssRule("thead th") {
        Modifier
            .fontWeight(FontWeight.Bold)
            // Разделитель заголовка отделяем от разделителей строк, чтобы шапка не читалась как обычная строка
            .borderBottom(
                width = 2.px,
                style = LineStyle.Solid,
                color = colorMode.toPalette().color.toRgb().copyf(alpha = 0.6f)
            )
    }

    // Чередование строк + подсветка под курсором: строку легче проследить целиком
    cssRule("tbody tr:nth-child(even)") {
        Modifier.backgroundColor(colorMode.toPalette().color.toRgb().copyf(alpha = 0.05f))
    }

    cssRule("tbody tr:hover") {
        Modifier.backgroundColor(colorMode.toPalette().color.toRgb().copyf(alpha = 0.1f))
    }
}

@InitRoute
fun initMarkdownLayout(ctx: InitRouteContext) {
    val title = ctx.markdown!!.frontMatter["title"]?.singleOrNull()
    require(title != null) { "Markdown file must set \"title\" in frontmatter" }

    ctx.data.add(PageLayoutData(title))
}

@Composable
@Layout(".components.layouts.PageLayout")
fun MarkdownLayout(content: @Composable () -> Unit) {
    Div(MarkdownStyle.toAttrs()) {
        content()
    }
}
