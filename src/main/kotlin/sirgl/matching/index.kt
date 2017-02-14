package sirgl.matching


interface InvertedIndex {
    fun add(tokenEntry: TokenEntry) // todo add payload

    fun find(queryTokens: List<String>, similarity: Similarity, count: Int = 10): SearchResult
}

interface Similarity {
    fun findSimilarity(queryTokens: List<String>, docTokens: List<String>, hits: Int): Float
}

class BagOfWordSimilarity : Similarity {
    override fun findSimilarity(queryTokens: List<String>, docTokens: List<String>, hits: Int): Float {
        val uniqueQueryTerms = queryTokens.distinct().size
        val uniqueDocTerms = docTokens.distinct().size
        return (hits * hits).toFloat() / (uniqueDocTerms * uniqueQueryTerms)
    }
}

class WordProximitySimilarity : Similarity {
    private val localMetric: ThreadLocal<DamerauLevensteinMetric> = ThreadLocal.withInitial { DamerauLevensteinMetric(32) }
    private val metric: DamerauLevensteinMetric
        get() = localMetric.get()

    private val fadingFactor = 0.2

    override fun findSimilarity(queryTokens: List<String>, docTokens: List<String>, hits: Int): Float {
        val distance = metric.distance(queryTokens, docTokens, 6).toFloat()
        return 1.0f / (1 * Math.exp(distance * fadingFactor).toFloat())
    }
}

class InMemoryInvertedIndex : InvertedIndex {
    private val index = mutableMapOf<String, MutableSet<TokenEntry>>()

    override fun find(queryTokens: List<String>, similarity: Similarity, count: Int): SearchResult {
        val hits = mutableMapOf<TokenEntry, Int>()
        queryTokens.distinct().forEach {
            val entries = index[it]
            entries?.forEach {
                val hitCount = hits.getOrPut(it, { 0 })
                hits[it] = hitCount + 1
            }
        }
        val rankedHits = hits.entries
                .map { SearchHit(it.key, similarity.findSimilarity(queryTokens, it.key.tokens, it.value)) }
                .sortedByDescending { it.score }
                .take(count)
        return SearchResult(rankedHits, queryTokens)
    }

    override fun add(tokenEntry: TokenEntry) {
        tokenEntry.tokens.forEach {
            index.getOrPut(it) {
                mutableSetOf()
            }.add(tokenEntry)
        }
    }
}

data class SearchResult(val hits: List<SearchHit>, val queryTokens: List<String>)

data class SearchHit(val tokenEntry: TokenEntry, val score: Float)