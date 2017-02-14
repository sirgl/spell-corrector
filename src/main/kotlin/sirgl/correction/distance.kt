package sirgl.correction

interface StringDistanceMetric {
    fun distance(userStr: String, dictStr: String) : Float
}

interface StringDistanceScorer {
    val influence: Float
    fun score(term: String, candidates: List<String>) : List<Scored<String>>
}

data class Scored<out T>(
        val item: T,
        val score: Float
)