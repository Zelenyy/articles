package io.github.zelenyy.components.widgets

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.ObjectFit
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.navigation.BasePath
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
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
 * @param orientation тип видео (Landscape 16:9 или Portrait 9:16). Определяет пропорции контейнера.
 * @param modifier модификаторы для контейнера (можно задать кастомную ширину, отступы и т.д.)
 */
@Composable
fun VideoPlayer(
    src: String,
    orientation: VideoOrientation = VideoOrientation.Landscape,
    modifier: Modifier = Modifier,
) {
    val resolvedSrc = BasePath.prependTo(src)

    // Пропорции контейнера
    val (ratioW, ratioH) = when (orientation) {
        VideoOrientation.Landscape -> 16 to 9
        VideoOrientation.Portrait -> 9 to 16
    }

    Div(
        attrs = modifier
            .fillMaxWidth()
            .aspectRatio(ratioW, ratioH)
            .borderRadius(12.px)
            .overflow(Overflow.Hidden)
            .toAttrs()
    ) {
        Video(
            attrs = Modifier
                .size(100.percent)
                .objectFit(ObjectFit.Cover)
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
