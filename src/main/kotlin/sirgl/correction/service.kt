package sirgl.correction

import com.github.liblevenshtein.transducer.Algorithm
import com.github.liblevenshtein.transducer.Candidate
import com.github.liblevenshtein.transducer.ITransducer
import com.github.liblevenshtein.transducer.factory.TransducerBuilder

class SpellCorrector(private val automaton: LevinsteinAutomaton, private val scorers: List<StringDistanceScorer>) {
    fun tryCorrect(term: String): String? {
        val candidates = automaton.correctFast(term)
                .map { it.term() }
        val results = scorers.map { ScorerData(it.score(term, candidates), it.influence) }
        val finalScores = Array(candidates.size, { 0.0f })
        for ((scorerScores, influence) in results) {
            for ((index,score) in scorerScores.withIndex()) {
                finalScores[index] += score.score * influence
            }
        }
        return pickMax(candidates, finalScores)
    }

    private fun pickMax(candidates: List<String>, scores: Array<Float>): String? {
        var max = Float.MIN_VALUE
        var maxIndex = -1
        for ((index, score) in scores.withIndex()) {
            if (max < score) {
                max = score
                maxIndex = index
            }
        }
        if (maxIndex == -1) {
            return null
        }
        return candidates[maxIndex]
    }

    data class ScorerData(
            val scores: List<Scored<String>>,
            val influence: Float
    )
}

class LevinsteinAutomaton(val dictionary: List<String>, val maxDistance: Int = 2) {
    fun correctFast(stemmedToken: String): List<Candidate> {
        val transducer: ITransducer<Candidate> = TransducerBuilder()
                .algorithm(Algorithm.TRANSPOSITION)
                .defaultMaxDistance(maxDistance)
                .includeDistance(true)
                .dictionary(dictionary)
                .build()
        return transducer.transduce(stemmedToken).toList()
    }
}
