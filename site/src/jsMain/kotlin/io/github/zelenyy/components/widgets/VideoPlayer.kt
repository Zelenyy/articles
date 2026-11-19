package io.github.zelenyy.components.widgets

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.ObjectFit
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.navigation.BasePath
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Source
import org.jetbrains.compose.web.dom.Video

enum class VideoOrientation {
    /** Горизонтальное видео (16:9) — стандарт для YouTube, YouTube Shorts не подходит */
    Landscape,

    /** Вертикальное видео (9:16) — YouTube Shorts, TikTok, Reels */
    Portrait
}

/**
 * Адаптивный видеоплеер, поддерживающий как горизонтальные, так и вертикальные видео.
 *
 * @param src путь к видеофайлу
 * @param orientation тип видео. [VideoOrientation.Landscape] растягивает плеер на всю ширину
 *   контента с пропорциями 16:9; [VideoOrientation.Portrait] не задаёт пропорций вообще —
 *   `<video>` сам сохраняет своё реальное соотношение сторон, а размер ограничивается
 *   сверху по высоте вьюпорта, чтобы вертикальное видео не выползало за экран.
 * @param modifier модификаторы для внешнего блока (в Landscape — для контейнера-обёртки,
 *   в Portrait — для самого `<video>`)
 */
@Composable
fun VideoPlayer(
    src: String,
    orientation: VideoOrientation = VideoOrientation.Landscape,
    modifier: Modifier = Modifier,
) {
    val resolvedSrc = BasePath.prependTo(src)

    when (orientation) {
        VideoOrientation.Landscape -> Div(
            attrs = modifier
                .fillMaxWidth()
                .aspectRatio(16, 9)
                .borderRadius(12.px)
                .overflow(Overflow.Hidden)
                .toAttrs()
        ) {
            Video(
                attrs = Modifier
                    .size(100.percent)
                    // Contain, а не Cover: если реальное соотношение сторон видео отличается
                    // от 16:9, границы не обрезаются — появляются поля по краям.
                    .objectFit(ObjectFit.Contain)
                    .toAttrs {
                        attr("controls", "")
                        attr("preload", "metadata")
                    }
            ) {
                Source(attrs = {
                    attr("src", resolvedSrc)
                    attr("type", "video/mp4")
                })
            }
        }

        // Пропорции не задаём: реальное соотношение сторон может быть любым
        // (например 576x742 ≈ 0.78, а не 9:16). Ограничиваем только габариты.
        VideoOrientation.Portrait -> Video(
            attrs = modifier
                .maxWidth(100.percent)
                .maxHeight(80.vh)
                .borderRadius(12.px)
                .styleModifier {
                    // margin-inline: auto работает только на блочном элементе
                    property("display", "block")
                    property("margin-inline", "auto")
                }
                .toAttrs {
                    attr("controls", "")
                    attr("preload", "metadata")
                }
        ) {
            Source(attrs = {
                attr("src", resolvedSrc)
                attr("type", "video/mp4")
            })
        }
    }
}
