package sirgl.matching

import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.en.PorterStemFilter
import org.apache.lucene.analysis.snowball.SnowballFilter
import org.apache.lucene.analysis.standard.StandardTokenizer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute
import java.io.StringReader
import java.util.*

object Normalizer {
    fun normalize(query: String): TokenEntry {
        val tokens = tokenizeAndStem(query.toLowerCase()).distinct()
        return TokenEntry(query, tokens)
    }

    fun tokenizeAndStem(query: String): ArrayList<String> {
        val tokenStream: TokenStream = setupTokenStream(query)
        tokenStream.addAttribute(OffsetAttribute::class.java)
        val charTermAttr = tokenStream.getAttribute(CharTermAttribute::class.java)
        return extractTokens(tokenStream, charTermAttr)
    }

    private fun setupTokenStream(query: String): TokenStream {
        val tokenizer = StandardTokenizer()
        tokenizer.setReader(StringReader(query))
        var tokenStream: TokenStream = tokenizer

        tokenStream = SnowballFilter(tokenStream, "Russian")
        tokenStream = PorterStemFilter(tokenStream)
        return tokenStream
    }

    private fun extractTokens(tokenStream: TokenStream, charTermAttr: CharTermAttribute): ArrayList<String> {
        val tokens = ArrayList<String>()
        tokenStream.reset()
        while (tokenStream.incrementToken()) {
            tokens.add(charTermAttr.toString())
        }
        return tokens
    }
}

data class TokenEntry(
        val sentence: String,
        var tokens: List<String>)