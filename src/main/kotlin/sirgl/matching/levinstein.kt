package sirgl.matching

class DamerauLevensteinMetric(maxLength: Int) {
    private val DEFAULT_LENGTH = 255
    private var _currentRow: IntArray? = null
    private var _previousRow: IntArray? = null
    private var _transpositionRow: IntArray? = null


    init {
        _currentRow = IntArray(maxLength + 1)
        _previousRow = IntArray(maxLength + 1)
        _transpositionRow = IntArray(maxLength + 1)
    }

    /// Damerau-Levenshtein distance is computed in asymptotic time O((max + 1) * min(first.length(), second.length()))
    fun distance(first: List<Any>, second: List<Any>, max: Int): Int {
        var first = first
        var second = second
        var max = max
        var firstLength = first.size
        var secondLength = second.size

        if (firstLength == 0)
            return secondLength

        if (secondLength == 0) return firstLength

        if (firstLength > secondLength) {
            val tmp = first
            first = second
            second = tmp
            firstLength = secondLength
            secondLength = second.size
        }

        if (max < 0) max = secondLength
        if (secondLength - firstLength > max) return max + 1

        if (firstLength > _currentRow!!.size) {
            _currentRow = IntArray(firstLength + 1)
            _previousRow = IntArray(firstLength + 1)
            _transpositionRow = IntArray(firstLength + 1)
        }

        for (i in 0..firstLength)
            _previousRow!![i] = i

        var lastSecondCh : Any? = null
        for (i in 1..secondLength) {
            val secondCh = second[i - 1]
            _currentRow!![0] = i

            // Compute only diagonal stripe of width 2 * (max + 1)
            val from = Math.max(i - max - 1, 1)
            val to = Math.min(i + max + 1, firstLength)

            var lastFirstCh : Any? = null
            for (j in from..to) {
                val firstCh = first[j - 1]

                // Compute minimal cost of state change to current state from previous states of deletion, insertion and swapping
                val cost = if (firstCh == secondCh) 0 else 1
                var value = Math.min(Math.min(_currentRow!![j - 1] + 1, _previousRow!![j] + 1), _previousRow!![j - 1] + cost)

                // If there was transposition, take in account its cost
                if (firstCh == lastSecondCh && secondCh == lastFirstCh)
                    value = Math.min(value, _transpositionRow!![j - 2] + cost)

                _currentRow!![j] = value
                lastFirstCh = firstCh
            }
            lastSecondCh = secondCh

            val tempRow = _transpositionRow
            _transpositionRow = _previousRow
            _previousRow = _currentRow
            _currentRow = tempRow
        }

        return _previousRow!![firstLength]
    }
}