import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication
import com.varabyte.kobwebx.gradle.markdown.MarkdownBlock
import com.varabyte.kobwebx.gradle.markdown.MarkdownEntry

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kobweb.application)
    alias(libs.plugins.kobwebx.markdown)
}

group = "io.github.zelenyy"
version = "1.0-SNAPSHOT"

kobweb {
    app {
        index {
            description.set("Заметки")
        }
    }

    markdown {
        // Рубрикатор на главной генерируется из frontmatter Markdown-статей в `articles/`.
        process.set { entries ->
            ArticleCatalogGenerator.generate(this, entries)
        }
    }
}

kotlin {
    // This example is frontend only. However, for a fullstack app, you can uncomment the includeServer parameter
    // and the `jvmMain` source set below.
    configAsKobwebApplication("zelenyy" /*, includeServer = true*/)

    sourceSets {
//        commonMain.dependencies {
//          // Add shared dependencies between JS and JVM here if building a fullstack app
//        }

        jsMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.html.core)
            implementation(libs.kobweb.core)
            implementation(libs.kobweb.compose)
            implementation(libs.kobweb.silk)
            // This default template uses built-in SVG icons, but what's available is limited.
            // Uncomment the following if you want access to a large set of font-awesome icons:
            // implementation(libs.silk.icons.fa)
            implementation(libs.kobwebx.markdown)
        }

        // Uncomment the following if you pass `includeServer = true` into the `configAsKobwebApplication` call.
//        jvmMain.dependencies {
//            compileOnly(libs.kobweb.api) // Provided by Kobweb backend at runtime
//        }
    }
}

/**
 * Генерирует `ArticleCatalog.kt` (рубрикатор для главной страницы) из Markdown-файлов
 * в `src/jsMain/resources/markdown/articles/`.
 *
 * Рубрики определяются по директориям. Название рубрики берётся из frontmatter
 * `index.md`, лежащего в корне директории рубрики.
 *
 * Структура:
 *   articles/
 *     kotlin/
 *       index.md          ← frontmatter.title = название рубрики
 *       hello.md          ← статья
 *       another.md
 *     web/
 *       index.md
 *       post.md
 *
 * Поддерживаемые ключи frontmatter для статей:
 *  - `title`   — заголовок статьи (по умолчанию берётся из имени файла)
 *  - `summary` — краткое описание для карточки на главной
 *  - `date`    — дата в формате ГГГГ-ММ-ДД (сортировка: новые выше)
 */
object ArticleCatalogGenerator {
    private const val ARTICLES_PREFIX = "articles/"
    private const val RUBRIC_INDEX = "index.md"
    private const val DEFAULT_RUBRIC = "Разное"

    private data class Article(
        val title: String,
        val summary: String,
        val path: String,
        val date: String?,
    )

    /** "articles/kotlin/hello.md" → "kotlin" */
    private val MarkdownEntry.rubricSlug: String
        get() = filePath
            .removePrefix(ARTICLES_PREFIX)
            .split('/')
            .firstOrNull()
            ?: ""

    /** "kotlin" → "Kotlin" */
    private fun String.slugToTitle(): String =
        if (isEmpty()) DEFAULT_RUBRIC
        else split('-', '_').filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.replaceFirstChar(Char::uppercaseChar)
            }

    /** "articles/kotlin/index.md" → true */
    private val MarkdownEntry.isRubricIndex: Boolean
        get() = filePath.startsWith(ARTICLES_PREFIX) &&
                filePath.substringAfterLast('/') == RUBRIC_INDEX

    private fun String.toKotlinString(): String =
        "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

    private fun MarkdownEntry.frontMatterValue(key: String): String? =
        frontMatter[key]?.singleOrNull()?.trim()?.takeIf { it.isNotEmpty() }

    fun generate(scope: MarkdownBlock.ProcessScope, entries: List<MarkdownEntry>) {
        // Собираем отображение slug → название рубрики из frontmatter index.md
        val rubricTitles = entries
            .filter { it.isRubricIndex }
            .associate { it.rubricSlug to (it.frontMatterValue("title") ?: it.rubricSlug.slugToTitle()) }

        // Отбираем статьи, исключаем index.md
        val articleEntries = entries
            .filter { it.filePath.startsWith(ARTICLES_PREFIX) && it.filePath.endsWith(".md") && !it.isRubricIndex }

        // Сортируем: новые статьи выше; статьи без даты — в конце
        val sorted = articleEntries.sortedByDescending { it.frontMatterValue("date") ?: "" }

        // Группируем по рубрикам, сохраняя порядок по дате (рубрика самой свежей статьи — первой)
        val rubrics = LinkedHashMap<String, MutableList<Article>>()
        sorted.forEach { entry ->
            rubrics
                .getOrPut(entry.rubricSlug) { mutableListOf() }
                .add(
                    Article(
                        title = entry.frontMatterValue("title")
                            ?: entry.filePath.removePrefix(ARTICLES_PREFIX).removeSuffix(".md"),
                        summary = entry.frontMatterValue("summary") ?: "",
                        path = entry.route,
                        date = entry.frontMatterValue("date"),
                    )
                )
        }

        // Название рубрики: из index.md, иначе — из имени директории
        fun rubricTitle(slug: String) = rubricTitles[slug] ?: slug.slugToTitle()

        scope.generateKotlin("io/github/zelenyy/content/ArticleCatalog.kt", buildString {
            appendLine("package io.github.zelenyy.content")
            appendLine()
            appendLine("// DO NOT EDIT THIS FILE BY HAND! IT IS AUTO-GENERATED BY `site/build.gradle.kts`")
            appendLine("// from Markdown frontmatter under `resources/markdown/articles`.")
            appendLine()
            appendLine("data class Article(")
            appendLine("    val title: String,")
            appendLine("    val summary: String,")
            appendLine("    val path: String,")
            appendLine("    val date: String? = null,")
            appendLine(")")
            appendLine()
            appendLine("data class Rubric(")
            appendLine("    val title: String,")
            appendLine("    val articles: List<Article>,")
            appendLine(")")
            appendLine()
            appendLine("val ArticleCatalog: List<Rubric> = listOf(")
            rubrics.forEach { (slug, articles) ->
                appendLine("    Rubric(")
                appendLine("        title = ${rubricTitle(slug).toKotlinString()},")
                appendLine("        articles = listOf(")
                articles.forEach { article ->
                    appendLine("            Article(")
                    appendLine("                title = ${article.title.toKotlinString()},")
                    appendLine("                summary = ${article.summary.toKotlinString()},")
                    appendLine("                path = ${article.path.toKotlinString()},")
                    if (article.date != null) {
                        appendLine("                date = ${article.date.toKotlinString()},")
                    }
                    appendLine("            ),")
                }
                appendLine("        ),")
                appendLine("    ),")
            }
            appendLine(")")
        })

        // Логируем сгенерированный каталог
        println("Каталог статей:")
        rubrics.forEach { (slug, articles) ->
            println("  * ${rubricTitle(slug)} [$slug] — статей: ${articles.size}")
            articles.forEach { article -> println("      - ${article.title} (${article.date ?: "без даты"})") }
        }
    }
}
