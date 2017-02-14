package sirgl.correction

import java.nio.file.Path

fun min(a: Int, b : Int) = Math.min(a,b)

class FineDistanceScorer(override val influence: Float) : StringDistanceScorer {
    private val distance: ThreadLocal<StringDistanceMetric> = ThreadLocal.withInitial { FineEditDistance() }

    override fun score(term: String, candidates: List<String>): List<Scored<String>> {
        val metric = distance.get()
        return candidates.map { candidate -> Scored(candidate, metric.distance(term, candidate)) }
    }
}

//ported from c++, source https://habrahabr.ru/post/123320/
//Warning! Not thread safe!
class FineEditDistance : StringDistanceMetric {

    companion object {
        val cutoffFactor = 1 / 3.0f //factor regulating count of possible errors, depending on length of string
        val errorInfluenceFactor = 0.3f //factor regulating influence of errors for final score


        private val foneticMap = mutableMapOf<Char, Int>()
        private var counter = 0
        init {
            listOf(
                    "aeiouy",
                    "bp",
                    "ckq",
                    "dt",
                    "lr",
                    "mn",
                    "gj",
                    "fvw",
                    "sxz",
                    "sz",
                    //Russian
                    "аоу",
                    "еи"
            ).forEach { addFoneticGroup(it.toUpperCase()) }
        }

        fun addFoneticGroup(group: String) {
            group.forEach { foneticMap[it] = counter }
            counter++
        }

        fun replaceCost(first: Char, second: Char) : Int {
            if(first == second) {
                return 0
            }
            val foneticGroup1 = foneticMap[first]
            val foneticGroup2 = foneticMap[second]
            if(foneticGroup1 != null && foneticGroup2 != null && foneticGroup1 == foneticGroup2) {
                return 1
            }
            return 2
        }
    }

    override fun distance(userStr: String, dictStr: String): Float {
        val distance = damerauLevinstein(userStr, dictStr)
        val len = userStr.length
        val okPoint = cutoffFactor * len // max acceptable errors

        val score: Float =
        if(distance < okPoint) {
            1.0f - (errorInfluenceFactor * distance) / okPoint
        } else {
            2 * (1.0f - errorInfluenceFactor) / (1.0f + Math.exp((distance - okPoint).toDouble()).toFloat())
        }
        return score
    }

    val maxStrLength = 255

    val trace = Array(maxStrLength + 1, { Array(maxStrLength + 1, {0}) })

    fun damerauLevinstein(userStr: String, dictStr: String): Int {
        val userSize = userStr.length
        val dictSize = dictStr.length
        if(dictSize > maxStrLength) {
            throw IllegalAccessException("dict str is too big")
        }
        if(userSize > maxStrLength) {
            throw IllegalAccessException("user str is too big")
        }
        for(i in 0..userSize) {
            trace[i][0] = i shl 1
        }
        for(j in 0..dictSize) {
            trace[0][j] = j shl 1
        }
        for(j in 1..dictSize) {
            for(i in 1..userSize) {
                // Учтем вставки, удаления и замены
                val replaceCost = replaceCost(userStr[i - 1], dictStr[j - 1])
                val dist0 = trace[i - 1][j] + 2
                val dist1 = trace[i][j - 1] + 2
                val dist2 = trace[i - 1][j - 1] + replaceCost
                trace[i][j] = min(dist0, min(dist1, dist2))
                // Учтем обмен
                if (i > 1 && j > 1 &&
                        userStr[i - 1] == dictStr[j - 2] &&
                        userStr[i - 2] == dictStr[j - 1]) {
                    trace[i][j] = min(trace[i][j], trace[i - 2][j - 2] + 1)
                }
            }
        }
        // Возьмем минимальное
        // префиксное расстояние
        var minDist = trace[userSize][0]
        for (i in 1..dictSize) {
            if (trace[userSize][i] < minDist)
                minDist = trace[userSize][i]
        }
        return minDist
    }
}