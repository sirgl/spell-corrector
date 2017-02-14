package sirgl.correction.fonetic

import org.apache.commons.codec.language.Caverphone2
import sirgl.correction.Scored
import sirgl.correction.StringDistanceScorer

fun buildPhoneticDict(dict: List<String>): List<String> {
    val caverphone2 = Caverphone2()
    return dict.map { caverphone2.encode(it) }
}

class FoneticStringScorer(override val influence: Float) : StringDistanceScorer {
    override fun score(term: String, candidates: List<String>): List<Scored<String>> {
        val stemmed = term
        return buildPhoneticDict(candidates)
                .map {
                    val score = when (it) {
                        stemmed -> 1.0f
                        else -> 0.0f
                    }
                    Scored(it, score)
                }
    }

}