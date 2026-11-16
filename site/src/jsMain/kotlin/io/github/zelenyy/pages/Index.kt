package io.github.zelenyy.pages

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.data.add
import com.varabyte.kobweb.core.init.InitRoute
import com.varabyte.kobweb.core.init.InitRouteContext
import com.varabyte.kobweb.core.layout.Layout
import com.varabyte.kobweb.silk.components.layout.HorizontalDivider
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.base
import com.varabyte.kobweb.silk.style.toAttrs
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.palette.color
import com.varabyte.kobweb.silk.theme.colors.palette.toPalette
import io.github.zelenyy.components.layouts.PageLayoutData
import io.github.zelenyy.content.Article
import io.github.zelenyy.content.ArticleCatalog
import io.github.zelenyy.content.Rubric
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.dom.Div

val ArticleIndexStyle = CssStyle.base {
    Modifier.fillMaxWidth().gap(3.cssRem)
}

val RubricStyle = CssStyle.base {
    Modifier.fillMaxWidth().gap(0.75.cssRem)
}

val RubricTitleStyle = CssStyle.base {
    Modifier.fontSize(1.8.cssRem).fontWeight(FontWeight.Bold)
}

val ArticleRowStyle = CssStyle.base {
    Modifier.fillMaxWidth().padding(top = 1.cssRem, bottom = 1.cssRem).gap(0.25.cssRem)
}

val ArticleTitleStyle = CssStyle.base {
    Modifier.fontSize(1.15.cssRem)
}

val ArticleDateStyle = CssStyle.base {
    Modifier
        .fontSize(0.8.cssRem)
        .color(colorMode.toPalette().color.toRgb().copyf(alpha = 0.6f))
}

val ArticleSummaryStyle = CssStyle.base {
    Modifier
        .fontSize(0.95.cssRem)
        .color(colorMode.toPalette().color.toRgb().copyf(alpha = 0.8f))
}

val EmptyRubricStyle = CssStyle.base {
    Modifier
        .fontSize(0.95.cssRem)
        .padding(topBottom = 1.cssRem)
        .color(colorMode.toPalette().color.toRgb().copyf(alpha = 0.6f))
}

@InitRoute
fun initHomePage(ctx: InitRouteContext) {
    ctx.data.add(PageLayoutData("Главная"))
}

@Composable
private fun ArticleRow(article: Article) {
    Column(ArticleRowStyle.toModifier()) {
        Row(Modifier.fillMaxWidth().gap(0.75.cssRem), verticalAlignment = Alignment.CenterVertically) {
            Link(article.path, article.title, ArticleTitleStyle.toModifier())
            if (article.date != null) {
                SpanText(article.date, ArticleDateStyle.toModifier())
            }
        }
        SpanText(article.summary, ArticleSummaryStyle.toModifier())
    }
}

@Composable
private fun RubricSection(rubric: Rubric) {
    Column(RubricStyle.toModifier()) {
        Div(RubricTitleStyle.toAttrs()) {
            SpanText(rubric.title)
        }

        if (rubric.articles.isEmpty()) {
            Div(EmptyRubricStyle.toAttrs()) {
                SpanText("Пока нет статей")
            }
        } else {
            rubric.articles.forEachIndexed { index, article ->
                ArticleRow(article)
                if (index != rubric.articles.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Page
@Layout(".components.layouts.PageLayout")
@Composable
fun HomePage() {
    Column(ArticleIndexStyle.toModifier()) {
//        Column(Modifier.gap(1.cssRem)) {
//            Div(HeadlineTextStyle.toAttrs()) {
//                SpanText("Заметки")
//            }
//            Div(SubheadlineTextStyle.toAttrs()) {
//                SpanText("Статьи Михаила Зеленого о Kotlin, Compose и веб-разработке.")
//            }
//        }

        ArticleCatalog.forEach { rubric ->
            RubricSection(rubric)
        }
    }
}
