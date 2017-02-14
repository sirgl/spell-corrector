package sirgl

import au.com.bytecode.opencsv.CSVReader
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.provider
import sirgl.matching.Matcher
import java.io.InputStreamReader



object binds {
    val modelMatcher = "model matcher"
    val modelsRow = "models row"
    val models = "models"

}
val kodein = Kodein {
    bind<List<Array<String>>>(binds.modelsRow) with provider({

        val stream = this.javaClass.classLoader.getResourceAsStream("modifications.csv")
        val reader = CSVReader(InputStreamReader(stream))
        reader.readAll()
    })

    bind<List<String>>(binds.models) with provider {
        instance<List<Array<String>>>(binds.modelsRow)
                .map { it.joinToString(separator = " ") }
    }

    bind<Matcher>(binds.modelMatcher) with provider {
        Matcher.Builder(
                synonymToWord = emptyMap(),
                dictionary = instance(binds.models)
        ).build()
    }
}