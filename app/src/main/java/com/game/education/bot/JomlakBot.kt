package com.game.education.bot

import android.content.Context
import com.game.education.api.ApiInterface
import com.game.education.api.RetrofitClient
import com.game.education.api.ServerData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class JomlakBot(
    private val context: Context,
    private var token: String? = null,
    private val onLog: (String) -> Unit = {}
) {

    private val api: ApiInterface =
        RetrofitClient.getRetrofitInstance().create(ApiInterface::class.java)
    private val progress: ProgressManager = ProgressManager(context)

    private val delayMs: Long = 300L
    private val wordDelayMs: Long = 80L
    private val targetTitleIds = listOf(1, 2, 3, 4)

    val solvedWords = mutableListOf<SolvedItem>()
    val solvedSentences = mutableListOf<SolvedItem>()
    val unresolvedSentences = mutableListOf<UnsolvedItem>()
    val allWordsInAllSentences = mutableListOf<String>()
    val stuckReport = mutableListOf<String>()

    data class SolvedItem(val sentenceId: Int, val text: String, val timestamp: String)
    data class UnsolvedItem(val sentenceId: Int, val text: String, val reason: String, val timestamp: String)

    data class ParsedSentence(
        val index: Int,
        val text: String,
        val separator: Char?,
        val words: List<String>,
        val wordCount: Int
    )

    data class ParsedTextResult(
        val words: List<String>,
        val sentences: List<ParsedSentence>,
        val fullText: String,
        val sentenceCount: Int,
        val wordCount: Int,
        val uniqueWords: List<String>
    )

    private fun log(message: String) {
        onLog(message)
    }

    private fun formatPersian(text: String?): String {
        if (text.isNullOrEmpty()) return ""
        return "\u200F$text"
    }

    private fun getSentenceText(sentence: ServerData.Sentence?): String {
        if (sentence == null) return "No text available"
        val title = sentence.title
        return if (!title.isNullOrEmpty()) title else "No text available"
    }

    private fun makeSentenceEditLink(sentenceId: Int, text: String): String {
        val cleanText = text.ifEmpty { "بدون متن" }
        val editUrl = "https://unusualteaching.ir/sentences/$sentenceId/edit"
        return "متن جمله: \"$cleanText\" (لینک ویرایش: $editUrl)"
    }

    private fun validateSentence(sentenceId: Int, text: String): String? {
        if (text.isBlank() || text == "No text available") {
            return "جمله با شناسه $sentenceId فاقد متن است. " + makeSentenceEditLink(sentenceId, text)
        }

        val words = text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        val wordCount = words.size
        val hasComma = text.contains("،") || text.contains(",")

        if (wordCount > 8 && !hasComma) {
            return "عبارت بدون ویرگول بیش از حد طولانی است. یک عبارت $wordCount کلمه دارد و با ویرگول جدا نشده است (حداکثر 8 کلمه). " + makeSentenceEditLink(sentenceId, text)
        } else if (wordCount < 2) {
            return "عبارت بسیار کوتاه است ($wordCount کلمه - کمتر از ۲ کلمه). " + makeSentenceEditLink(sentenceId, text)
        }

        return null
    }

    fun parsePersianText(text: String?): ParsedTextResult {
        if (text.isNullOrEmpty() || text == "No text available") {
            return ParsedTextResult(emptyList(), emptyList(), text ?: "", 0, 0, emptyList())
        }

        val normalized = text
            .replace('\u00A0', ' ')
            .replace('\u2009', ' ')
            .replace('\u202F', ' ')
            .replace(Regex("[\\t\\n\\r]+"), " ")
            .replace("\u200F", "")
            .replace("\u200E", "")

        val sentenceSeparators = setOf('،', ',', '؛', ';', '.', '!', '?', '؟')
        val rawSentences = mutableListOf<Pair<String, Char?>>()
        val currentSentence = StringBuilder()

        for (char in normalized) {
            if (char in sentenceSeparators) {
                val sentStr = currentSentence.toString().trim()
                if (sentStr.isNotEmpty()) {
                    rawSentences.add(Pair(sentStr, char))
                }
                currentSentence.clear()
            } else {
                currentSentence.append(char)
            }
        }

        val remaining = currentSentence.toString().trim()
        if (remaining.isNotEmpty()) {
            rawSentences.add(Pair(remaining, null))
        }

        val sentences = rawSentences.mapIndexed { index, pair ->
            val words = pair.first
                .split(Regex(" +"))
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            ParsedSentence(
                index = index + 1,
                text = pair.first,
                separator = pair.second,
                words = words,
                wordCount = words.size
            )
        }

        val allWords = mutableListOf<String>()
        sentences.forEach { s ->
            allWords.addAll(s.words)
        }

        return ParsedTextResult(
            words = allWords,
            sentences = sentences,
            fullText = normalized,
            sentenceCount = sentences.size,
            wordCount = allWords.size,
            uniqueWords = allWords.distinct()
        )
    }

    private fun displayParsedText(sentenceId: Int, text: String): ParsedTextResult {
        log("\n" + "═".repeat(50))
        log("📝 شناسه جمله: $sentenceId")
        log("═".repeat(50))
        log("📖 متن کامل: ${formatPersian(text)}")
        log("─".repeat(50))

        val parsed = parsePersianText(text)

        log("\n📚 تعداد جملات/عبارات: ${parsed.sentenceCount} | کل کلمات: ${parsed.wordCount}")
        log("─".repeat(50))

        parsed.sentences.forEachIndexed { i, sent ->
            log("\n  ${i + 1}. جمله/عبارت ${i + 1}:")
            log("     📖 متن: ${formatPersian(sent.text)}")
            if (sent.separator != null) {
                log("     🔚 جداکننده: \"${sent.separator}\"")
            }
            log("     🔤 کلمات این جمله (${sent.wordCount}):")
            sent.words.forEachIndexed { j, w ->
                log("        ${j + 1}. ${formatPersian(w)}")
            }
        }

        log("─".repeat(50) + "\n")
        return parsed
    }

    private suspend fun solveWordByWord(sentenceId: Int, sentenceText: String, parsed: ParsedTextResult): List<Boolean> {
        log("\n🔤 شروع حل کلمه به کلمه برای جمله $sentenceId...")
        log("─".repeat(45))

        val results = mutableListOf<Boolean>()
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        for (word in parsed.words) {
            log("\n  🔤 حل کلمه: ${formatPersian(word)}")

            val success = completeSentence(sentenceId)
            if (success) {
                val itemText = "کلمه: \"$word\" | " + makeSentenceEditLink(sentenceId, sentenceText)
                solvedWords.add(SolvedItem(sentenceId, itemText, timeStamp))
                results.add(true)
                log("  ✅ کلمه \"${formatPersian(word)}\" با موفقیت حل شد")
            } else {
                results.add(false)
                log("  ❌ کلمه \"${formatPersian(word)}\" حل نشد")
            }

            delay(wordDelayMs)
        }

        return results
    }

    private suspend fun solveSentenceBySentence(
        sentenceId: Int,
        sentenceText: String,
        parsed: ParsedTextResult
    ): List<Boolean> {
        log("\n📖 شروع حل جمله به جمله برای جمله $sentenceId...")
        log("─".repeat(45))

        val results = mutableListOf<Boolean>()
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        for (sentObj in parsed.sentences) {
            val sentText = sentObj.text

            log("\n  📖 حل جمله/عبارت: ${formatPersian(sentText)}")
            log("     (شامل ${sentObj.wordCount} کلمه)")

            val success = completeSentence(sentenceId)
            if (success) {
                val itemText = makeSentenceEditLink(sentenceId, sentText)
                solvedSentences.add(SolvedItem(sentenceId, itemText, timeStamp))
                results.add(true)
                log("  ✅ جمله/عبارت با موفقیت حل شد")
            } else {
                results.add(false)
                log("  ❌ جمله/عبارت حل نشد")
            }

            delay(wordDelayMs)
        }

        return results
    }

    private suspend fun solveSentenceCompletely(sentenceId: Int, sentenceText: String): Boolean {
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val validationError = validateSentence(sentenceId, sentenceText)
        if (validationError != null) {
            reportStuck("word_validation_error", validationError)
            unresolvedSentences.add(
                UnsolvedItem(
                    sentenceId = sentenceId,
                    text = sentenceText,
                    reason = validationError,
                    timestamp = timeStamp
                )
            )
            return false
        }

        log("\n" + "█".repeat(50))
        log("🎯 حل کامل جمله $sentenceId")
        log("█".repeat(50))

        val parsed = displayParsedText(sentenceId, sentenceText)
        allWordsInAllSentences.addAll(parsed.words)

        val wordResults = solveWordByWord(sentenceId, sentenceText, parsed)
        val sentenceResults = solveSentenceBySentence(sentenceId, sentenceText, parsed)

        log("\n📖 حل کل جمله اصلی...")
        val fullSuccess = completeSentence(sentenceId)

        if (fullSuccess) {
            log("✅ کل جمله $sentenceId با موفقیت حل شد")
        } else {
            log("❌ حل کل جمله $sentenceId ناموفق بود")
            unresolvedSentences.add(
                UnsolvedItem(
                    sentenceId = sentenceId,
                    text = sentenceText,
                    reason = "خطا در درخواست تکمیل نهایی سرور (completeSentence ناموفق بود). " + makeSentenceEditLink(sentenceId, sentenceText),
                    timestamp = timeStamp
                )
            )
        }

        val wordSuccessCount = wordResults.count { it }
        val sentSuccessCount = sentenceResults.count { it }

        log("\n📊 خلاصه حل جمله $sentenceId:")
        log("   🔤 کلمات: $wordSuccessCount/${wordResults.size} موفق")
        log("   📖 جملات/عبارات: $sentSuccessCount/${sentenceResults.size} موفق")
        log("   📝 کل جمله: ${if (fullSuccess) "✅" else "❌"}")

        return fullSuccess
    }

    private fun reportStuck(type: String, details: String) {
        val msg = "گیر کرده در $type: $details"
        stuckReport.add(msg)
        log("⚠️ $msg")
    }

    fun generateReport(): String {
        val reportBuilder = StringBuilder()
        reportBuilder.append("📋 گزارش جامع و عملکرد اجرای جاری ربات جملک\n")
        reportBuilder.append("=====================================\n")

        reportBuilder.append("🔤 کلمات حل شده در این اجرا: ${solvedWords.size}\n")
        reportBuilder.append("📖 جملات/عبارات حل شده در این اجرا: ${solvedSentences.size}\n")
        reportBuilder.append("❌ جملات حل‌نشده و دارای خطای اعتبارسنجی (>8 کلمه بدون ویرگول / خطاها): ${unresolvedSentences.size}\n")
        reportBuilder.append("📚 کل کلمات پردازش شده در این اجرا: ${allWordsInAllSentences.size}\n")
        reportBuilder.append("⚠️ تعداد خطاهای ساختاری و عناوین خالی (Stuck): ${stuckReport.size}\n\n")



        if (unresolvedSentences.isNotEmpty()) {
            reportBuilder.append("--- ❌ لیست جملات حل‌نشده و خطاهای اعتبارسنجی (بیش از8  کلمه بدون ویرگول / خطاها) ---\n")
            unresolvedSentences.forEachIndexed { index, item ->
                reportBuilder.append("${index + 1}. شناسه جمله: ${item.sentenceId}\n")
                reportBuilder.append("   دلیل / خطا: ${item.reason}\n")
            }
            reportBuilder.append("\n")
        } else {
            reportBuilder.append("🎉 هیچ خطای اعتبارسنجی یا جمله حل‌نشده‌ای در این اجرا گزارش نشد!\n\n")
        }

        if (stuckReport.isNotEmpty()) {
            reportBuilder.append("--- لیست خطاها، هشدارها و نواقص ساختاری عناوین/زیرعناوین ---\n")
            stuckReport.forEachIndexed { index, err ->
                reportBuilder.append("${index + 1}. $err\n")
            }
        } else {
            reportBuilder.append("🎉 هیچ خطایی در عناوین، زیرعناوین یا تعداد کلمات گزارش نشد!\n")
        }

        val finalReportText = reportBuilder.toString()
        log(finalReportText)
        return finalReportText
    }

    suspend fun run() {
        withContext(Dispatchers.IO) {
            solvedWords.clear()
            solvedSentences.clear()
            unresolvedSentences.clear()
            allWordsInAllSentences.clear()
            stuckReport.clear()

            log("🎮 ربات جملک - شروع اجرای تازه")
            log("🎯 عناوین هدف: $targetTitleIds")
            log("🔬 حالت: حل کلمه به کلمه + جمله به جمله از صفر")
            log("=".repeat(50))

            try {
                val allTitles = getTitles()
                log("\n📚 تعداد کل عناوین دریافت شده: ${allTitles.size}")

                if (allTitles.isEmpty()) {
                    reportStuck("empty_titles_response", "هیچ عنوانی از سرور دریافت نشد یا لیست خالی است.")
                }

                val targetTitles = allTitles.filter { it.id in targetTitleIds }
                log("🎯 عناوین انتخاب شده برای حل: ${targetTitles.size}")

                if (targetTitles.isEmpty()) {
                    reportStuck("empty_target_titles", "هیچ‌کدام از عناوین هدف با شناسه‌های $targetTitleIds در لیست سرور یافت نشدند.")
                }

                for (title in targetTitles) {
                    val titleId = title.id ?: continue
                    val titleName = title.title ?: "بدون عنوان"
                    log("\n" + "█".repeat(50))
                    log("📚 عنوان [$titleId] $titleName")
                    log("█".repeat(50))

                    val categories = getCategories(titleId)
                    log("📂 تعداد دسته‌بندی‌ها: ${categories.size}")

                    if (categories.isEmpty()) {
                        reportStuck(
                            "empty_categories",
                            "عنوان [شناسه: $titleId، نام: '$titleName'] فاقد هرگونه دسته‌بندی (Category) است."
                        )
                        continue
                    }

                    var totalSentences = 0
                    var completedCount = 0

                    for (category in categories) {
                        val catId = category.id ?: continue
                        val catName = category.name ?: category.title ?: "بدون نام"
                        log("\n  📂 دسته [$catId] $catName")

                        val bases = getBases(catId)
                        if (bases.isEmpty()) {
                            reportStuck(
                                "empty_bases",
                                "دسته‌بندی [شناسه: $catId، نام: '$catName'] تحت عنوان [شناسه: $titleId، نام: '$titleName'] فاقد پایه (Base) است."
                            )
                            continue
                        }

                        for (base in bases) {
                            val baseId = base.id ?: continue
                            val baseName = base.name ?: base.title ?: "بدون نام"
                            log("\n    📁 پایه [$baseId] $baseName")

                            val chapters = getChapters(baseId)
                            if (chapters.isEmpty()) {
                                reportStuck(
                                    "empty_chapters",
                                    "پایه [شناسه: $baseId، نام: '$baseName'] در دسته [شناسه: $catId، نام: '$catName'] فاقد فصل (Chapter) است."
                                )
                                continue
                            }

                            for (chapter in chapters) {
                                val chapterId = chapter.id ?: continue
                                val chapterName = chapter.name ?: chapter.title ?: "بدون نام"
                                log("\n      📖 فصل [$chapterId] $chapterName")

                                val sentences = getSentences(chapterId)
                                log("      تعداد جملات فصل: ${sentences.size}")

                                if (sentences.isEmpty()) {
                                    reportStuck(
                                        "empty_sentences",
                                        "فصل [شناسه: $chapterId، نام: '$chapterName'] تحت پایه [شناسه: $baseId، نام: '$baseName'] فاقد هرگونه جمله است."
                                    )
                                    continue
                                }

                                totalSentences += sentences.size

                                for (sentence in sentences) {
                                    val sentenceId = sentence.id ?: continue
                                    val text = getSentenceText(sentence)

                                    val success = solveSentenceCompletely(sentenceId, text)
                                    if (success) {
                                        completedCount++
                                    }

                                    delay(delayMs)
                                }
                            }
                        }
                    }

                    log("\n📊 خلاصه عنوان [$titleId] $titleName: جملات حل‌شده $completedCount/$totalSentences")
                }

                log("\n🎉 عملیات کاوش و حل به پایان رسید!")
                generateReport()

            } catch (e: Exception) {
                log("❌ خطای غیرمنتظره در ربات: ${e.message}")
                generateReport()
            }
        }
    }

    private suspend fun getTitles(): List<ServerData.Title> = withContext(Dispatchers.IO) {
        try {
            val response = api.titles("fa").execute()
            if (response.isSuccessful) {
                val body: Any? = response.body()
                when (body) {
                    is ServerData.TitleResponse -> body.result ?: emptyList()
                    is ServerData.levels -> body.items?.map { item ->
                        ServerData.Title().apply {
                            this.id = item.id
                            this.title = item.title
                        }
                    } ?: emptyList()

                    else -> emptyList()
                }
            } else {
                log("❌ خطای دریافت عناوین: کد ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            log("❌ استثنا در دریافت عناوین: ${e.localizedMessage}")
            emptyList()
        }
    }

    private suspend fun getCategories(titleId: Int): List<ServerData.Category> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.categories(titleId, "fa").execute()
                if (response.isSuccessful) {
                    val body: Any? = response.body()
                    when (body) {
                        is ServerData.CategoryResponse -> body.result ?: emptyList()
                        is ServerData.levels -> body.items?.map { item ->
                            ServerData.Category().apply {
                                this.id = item.id
                                this.title = item.title
                                this.titleId = titleId
                            }
                        } ?: emptyList()

                        else -> emptyList()
                    }
                } else {
                    log("❌ خطای دریافت دسته‌ها (Title $titleId): کد ${response.code()}")
                    emptyList()
                }
            } catch (e: Exception) {
                log("❌ استثنا در getCategories: ${e.localizedMessage}")
                emptyList()
            }
        }

    private suspend fun getBases(categoryId: Int): List<ServerData.Base> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.bases(categoryId, "fa").execute()
                if (response.isSuccessful) {
                    val body: Any? = response.body()
                    when (body) {
                        is ServerData.BaseResponse -> body.result ?: emptyList()
                        is ServerData.levels -> body.items?.map { item ->
                            ServerData.Base().apply {
                                this.id = item.id
                                this.title = item.title
                                this.categoryId = categoryId
                            }
                        } ?: emptyList()

                        else -> emptyList()
                    }
                } else {
                    log("❌ خطای دریافت پایه‌ها (Cat $categoryId): کد ${response.code()}")
                    emptyList()
                }
            } catch (e: Exception) {
                log("❌ استثنا در getBases: ${e.localizedMessage}")
                emptyList()
            }
        }

    private suspend fun getChapters(baseId: Int): List<ServerData.Chapter> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.chapters(baseId, "fa").execute()
                if (response.isSuccessful) {
                    val body: Any? = response.body()
                    when (body) {
                        is ServerData.ChapterResponse -> body.result ?: emptyList()
                        is ServerData.levels -> body.items?.map { item ->
                            ServerData.Chapter().apply {
                                this.id = item.id
                                this.title = item.title
                                this.baseId = baseId
                            }
                        } ?: emptyList()

                        else -> emptyList()
                    }
                } else {
                    log("❌ خطای دریافت فصل‌ها (Base $baseId): کد ${response.code()}")
                    emptyList()
                }
            } catch (e: Exception) {
                log("❌ استثنا در getChapters: ${e.localizedMessage}")
                emptyList()
            }
        }

    private suspend fun getSentences(chapterId: Int): List<ServerData.Sentence> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.sentences(chapterId, "fa").execute()
                if (response.isSuccessful) {
                    val body: Any? = response.body()
                    when (body) {
                        is ServerData.SentenceResponse -> body.items ?: body.result ?: emptyList()
                        is ServerData.levels -> body.items?.map { item ->
                            ServerData.Sentence().apply {
                                this.id = item.id
                                this.title = item.title
                                this.chapterId = chapterId
                            }
                        } ?: emptyList()

                        else -> emptyList()
                    }
                } else {
                    log("❌ خطای دریافت جملات فصل $chapterId: کد ${response.code()}")
                    emptyList()
                }
            } catch (e: Exception) {
                log("❌ استثنا در getSentences: ${e.localizedMessage}")
                emptyList()
            }
        }

    suspend fun completeSentence(sentenceId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val authToken = if (token?.startsWith("Bearer ") == true) token else "Bearer $token"
            val response = api.sentences_complete(
                authToken,
                mapOf("sentence_id" to sentenceId)
            ).execute()

            if (response.isSuccessful) {
                val body = response.body()
                body?.success ?: true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun sendEmailReport(emailUser: String, emailPass: String, emailTo: String?): Boolean = withContext(Dispatchers.IO) {
        try {
            val config = EmailSender.EmailConfig(
                user = emailUser,
                password = emailPass,
                to = emailTo ?: ""
            )

            val reportContent = generateReport()
            val subject = "گزارش جامع و عملکرد ربات جملک"
            val plainBody = reportContent
            val htmlBody = """
            <div dir="rtl" style="font-family: Tahoma, sans-serif; padding: 16px; background-color: #f9f9f9; border-radius: 8px;">
                <h2 style="color: #2e7d32; border-bottom: 2px solid #2e7d32; padding-bottom: 8px;">گزارش جامع ربات جملک</h2>
                <pre style="font-family: Tahoma, sans-serif; font-size: 13px; white-space: pre-wrap; word-wrap: break-word; line-height: 1.6;">${reportContent
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
            }</pre>
            </div>
        """.trimIndent()

            val result = EmailSender.sendReport(
                config = config,
                subject = subject,
                htmlBody = htmlBody,
                plainBody = plainBody
            )

            if (result.isSuccess) {
                log("📧 گزارش ایمیل با موفقیت ارسال شد به: $emailTo")
                true
            } else {
                val err = result.exceptionOrNull()?.message ?: "خطای ناشناخته"
                log("❌ خطا در ارسال ایمیل: $err")
                false
            }
        } catch (e: Exception) {
            log("❌ استثنا در ارسال ایمیل: ${e.message}")
            false
        }
    }

    fun login(username: String, password: String): Boolean {
        try {
            log("🔑 در حال ارسال درخواست ورود برای کاربر: $username")
            val response = api.login(username, password)
                .execute()
            if (response.isSuccessful && response.body()?.success == true) {
                val authToken = response.body()?.result?.token
                if (!authToken.isNullOrEmpty()) {
                    token = authToken
                    log("✅ ورود با موفقیت انجام شد!")
                    return true
                }
            }
            log("❌ ورود ناموفق: ${response.errorBody()?.string() ?: "خطای ناشناخته"}")
            return false
        } catch (e: Exception) {
            log("❌ خطای ارتباطی در ورود: ${e.message}")
            return false
        }
    }
}