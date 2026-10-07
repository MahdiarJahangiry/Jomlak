package com.game.education.bot

/**
 * تجزیه‌کننده متن فارسی
 *
 * قوانین:
 * 1. فاصله معمولی (space) = جداکننده کلمه
 * 2. نیم‌فاصله (ZWNJ U+200C) = بخشی از کلمه (جدا نمی‌کند)
 * 3. ویرگول (، ,) = پایان جمله
 * 4. نقطه (.) = پایان جمله
 * 5. ؛ ; ! ? ؟ = پایان جمله
 */
object PersianParser {

    data class ParsedSentence(
        val index: Int,
        val text: String,
        val separator: Char?,
        val words: List<String>
    )

    data class ParsedText(
        val fullText: String,
        val sentences: List<ParsedSentence>,
        val allWords: List<String>,
        val uniqueWords: List<String>
    ) {
        val sentenceCount: Int get() = sentences.size
        val wordCount: Int get() = allWords.size
    }

    private val SENTENCE_SEPARATORS = setOf(
        '\u060C', // ، ویرگول فارسی
        ',',      // ویرگول انگلیسی
        '\u061B', // ؛ نقطه‌ویرگول فارسی
        ';',      // نقطه‌ویرگول انگلیسی
        '.',      // نقطه
        '!',      // تعجب
        '?',      // سوال انگلیسی
        '\u061F'  // ؟ سوال فارسی
    )

    fun parse(text: String): ParsedText {
        if (text.isEmpty()) {
            return ParsedText("", emptyList(), emptyList(), emptyList())
        }

        // مرحله 1: نرمال‌سازی (نیم‌فاصله حفظ می‌شود)
        val normalized = text
            .replace('\u00A0', ' ')
            .replace('\u2009', ' ')
            .replace('\u202F', ' ')
            .replace("\u200F", "")
            .replace("\u200E", "")
            .replace(Regex("[\\t\\n\\r]+"), " ")
            .trim()

        // مرحله 2: تقسیم به جملات
        val rawSentences = mutableListOf<Pair<String, Char?>>()
        val current = StringBuilder()

        for (char in normalized) {
            if (char in SENTENCE_SEPARATORS) {
                val t = current.toString().trim()
                if (t.isNotEmpty()) {
                    rawSentences.add(Pair(t, char))
                }
                current.clear()
            } else {
                current.append(char)
            }
        }

        val remaining = current.toString().trim()
        if (remaining.isNotEmpty()) {
            rawSentences.add(Pair(remaining, null))
        }

        // مرحله 3: تقسیم هر جمله به کلمات (فقط با فاصله)
        val sentences = rawSentences.mapIndexed { index, pair ->
            val words = pair.first
                .split(' ')
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            ParsedSentence(
                index = index + 1,
                text = pair.first,
                separator = pair.second,
                words = words
            )
        }

        // مرحله 4: جمع‌آوری کلمات
        val allWords = mutableListOf<String>()
        sentences.forEach { sent ->
            allWords.addAll(sent.words)
        }

        return ParsedText(
            fullText = normalized,
            sentences = sentences,
            allWords = allWords,
            uniqueWords = allWords.distinct()
        )
    }
}