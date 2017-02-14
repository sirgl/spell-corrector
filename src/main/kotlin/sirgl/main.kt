package sirgl

import com.github.salomonbrys.kodein.instance
import sirgl.matching.Matcher

fun main(args: Array<String>) {
    val matcher = kodein.instance<Matcher>(binds.modelMatcher)
    matcher.match(listOf("mazda", "demio"))
            .forEach(::println)
}