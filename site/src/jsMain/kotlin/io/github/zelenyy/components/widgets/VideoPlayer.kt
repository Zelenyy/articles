package io.github.zelenyy.components.widgets

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.navigation.BasePath
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Source
import org.jetbrains.compose.web.dom.Video

@Composable
fun VideoPlayer(src: String, modifier: Modifier = Modifier) {
    val resolvedSrc = BasePath.prependTo(src)

    Video(
        attrs = modifier
            .maxHeight(550.px)
            .maxWidth(100.percent)
            .borderRadius(12.px)
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
