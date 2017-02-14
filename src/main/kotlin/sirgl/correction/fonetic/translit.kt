package sirgl.correction.fonetic

// https://habrahabr.ru/post/265455/
object Translit {
    fun cyr2lat(ch: Char): String {
        when (ch) {
            'а' -> return "a"
            'б' -> return "b"
            'в' -> return "v"
            'г' -> return "g"
            'д' -> return "d"
            'е' -> return "e"
            'ё' -> return "je"
            'ж' -> return "zh"
            'з' -> return "z"
            'и' -> return "i"
            'й' -> return "y"
            'к' -> return "k"
            'л' -> return "l"
            'м' -> return "m"
            'н' -> return "n"
            'о' -> return "o"
            'п' -> return "p"
            'р' -> return "r"
            'с' -> return "s"
            'т' -> return "t"
            'у' -> return "u"
            'ф' -> return "f"
            'х' -> return "kh"
            'ц' -> return "c"
            'ч' -> return "ch"
            'ш' -> return "sh"
            'щ' -> return "jsh"
            'ъ' -> return "hh"
            'ы' -> return "ih"
            'ь' -> return "jh"
            'э' -> return "eh"
            'ю' -> return "ju"
            'я' -> return "ja"
            else -> return ch.toString()
        }
    }

    fun cyr2lat(s: String): String {
        val sb = StringBuilder(s.length * 2)
        for (ch in s.toCharArray()) {
            sb.append(cyr2lat(ch))
        }
        return sb.toString()
    }
}