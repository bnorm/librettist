package dev.bnorm.librettist.show

fun buildSlides(builder: ShowBuilder.() -> Unit): List<Slide> {
    val slides = buildList {
        object : ShowBuilder {
            override fun slide(advancements: Int, content: SlideContent<Int>) {
                require(advancements >= 1)
                add(Slide(advancements, content))
            }
        }.builder()
    }
    return slides
}

class Slide(
    val advancements: Int,
    val content: SlideContent<Int>,
)

val List<Slide>.advancements: List<Pair<Int, Int>>
    get() = flatMapIndexed { index, slide -> (0..<slide.advancements).map { index to it } }