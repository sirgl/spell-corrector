package sirgl.matching

import sirgl.correction.*
import sirgl.correction.fonetic.FoneticStringScorer

class Matcher(
        val synonymToWord: Map<String, String>,
        unifiedDictionary: List<String>,
        val index: InvertedIndex,
        stringDistanceScorers: List<StringDistanceScorer> = listOf(
                FoneticStringScorer(influence = 5f),
                FineDistanceScorer(influence = 95f)
        ),
        val wordProximityInfluenceFactor: Float = 0.2f
) {
    val spellCorrector: SpellCorrector = SpellCorrector(LevinsteinAutomaton(unifiedDictionary), stringDistanceScorers)


    fun match(userQuery: List<String>, count: Int = 10): MutableList<SearchHit> {
        val normalizedTokens = userQuery
                .map(String::toLowerCase)
                .map { spellCorrector.tryCorrect(it) ?: it }
                .map { synonymToWord[it] ?: it }
        val roughResult = index.find(normalizedTokens, BagOfWordSimilarity(), count)
        val proximitySimilarity = WordProximitySimilarity()
        val proximityScores = findProximityScores(proximitySimilarity, roughResult)
        return mergeSimilarityResults(proximityScores, roughResult)
    }

    private fun mergeSimilarityResults(proximityScores: List<Float>, roughResult: SearchResult): MutableList<SearchHit> {
        val hits = mutableListOf<SearchHit>()
        for ((index, hit) in roughResult.hits.withIndex()) {
            val newScore = hit.score * (1 - wordProximityInfluenceFactor) +
                    proximityScores[index] * wordProximityInfluenceFactor
            hits.add(SearchHit(hit.tokenEntry, newScore))
        }
        return hits
    }

    private fun findProximityScores(proximitySimilarity: WordProximitySimilarity, roughResult: SearchResult): List<Float> {
        return roughResult.hits.map {
            proximitySimilarity.findSimilarity(roughResult.queryTokens, it.tokenEntry.tokens, 0)
        }
    }


    class Builder(
            val synonymToWord: Map<String, String>,
            val dictionary: List<String>,
            val normalize: (String) -> TokenEntry = { Normalizer.normalize(it) }
    ) {
        fun build(): Matcher {
            val synonymDict = normalizeSynonyms()
            val normedDict = dictionary.map { normalize(it) }
            val unifiedDictionary = unifyDictionaries(synonymDict, normedDict)
            val index = makeIndex(normedDict)
            return Matcher(synonymDict, unifiedDictionary, index)
        }

        fun makeIndex(normedDict: List<TokenEntry>): InvertedIndex {
            val index = InMemoryInvertedIndex()
            normedDict.forEach { index.add(it) }
            return index
        }

        fun normalizeSynonyms() = synonymToWord
                .mapKeys { normalize(it.key).tokens.first() }
                .mapValues { normalize(it.value).tokens.first() }

        fun unifyDictionaries(synonymDict: Map<String, String>, normedDict: List<TokenEntry>): List<String> {
            val unified = synonymDict.keys.toMutableSet()
            normedDict.flatMapTo(unified, TokenEntry::tokens)
            return unified.toList()
        }
    }
}