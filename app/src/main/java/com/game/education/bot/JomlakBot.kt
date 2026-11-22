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
    val allWordsInAllSentences = mutableListOf<String>()
    val stuckReport = mutableListOf<String>()

    data class SolvedItem(val sentenceId: Int, val text: String, val timestamp: String)

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

    // ============================================================
    // 🔗 لینک مستقیم ویرایش ادمین برای هر جمله
    // ============================================================
    private fun makeSentenceEditLink(sentenceId: Int, text: String): String {
        val cleanText = text.ifEmpty { "بدون متن" }
        val editUrl = "https://unusualteaching.ir/sentences/$sentenceId/edit"
        return "متن جمله: \"$cleanText\" (لینک ویرایش: $editUrl)"
    }

    // ============================================================
    // 🔍 اعتبارسنجی طول کلمات، کاما و محدودیت‌ها
    // ============================================================
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

    // ============================================================
    // 🔑 تجزیه متن فارسی به کلمات و جملات
    // ============================================================
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

    // ============================================================
    // 🔑 حل کلمه به کلمه
    // ============================================================
    private suspend fun solveWordByWord(sentenceId: Int, sentenceText: String, parsed: ParsedTextResult): List<Boolean> {
        log("\n🔤 شروع حل کلمه به کلمه برای جمله $sentenceId...")
        log("─".repeat(45))

        val results = mutableListOf<Boolean>()
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        for (word in parsed.words) {
            val wordKey = "${sentenceId}_word_${word}"

            if (progress.isWordSolved(wordKey)) {
                log("  ⏭️ کلمه \"${formatPersian(word)}\" قبلاً حل شده")
                results.add(true)
                continue
            }

            log("\n  🔤 حل کلمه: ${formatPersian(word)}")

            val success = completeSentence(sentenceId)
            if (success) {
                progress.markWordSolved(wordKey)
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

    // ============================================================
    // 🔑 حل جمله به جمله
    // ============================================================
    private suspend fun solveSentenceBySentence(
        sentenceId: Int,
        parsed: ParsedTextResult
    ): List<Boolean> {
        log("\n📖 شروع حل جمله به جمله برای جمله $sentenceId...")
        log("─".repeat(45))

        val results = mutableListOf<Boolean>()
        val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        for (sentObj in parsed.sentences) {
            val sentText = sentObj.text
            val sentKey = "${sentenceId}_sentence_${sentText}"

            if (progress.isSentencePartSolved(sentKey)) {
                log("  ⏭️ جمله/عبارت \"${formatPersian(sentText)}\" قبلاً حل شده")
                results.add(true)
                continue
            }

            log("\n  📖 حل جمله/عبارت: ${formatPersian(sentText)}")
            log("     (شامل ${sentObj.wordCount} کلمه)")

            val success = completeSentence(sentenceId)
            if (success) {
                progress.markSentencePartSolved(sentKey)
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

    // ============================================================
    // 🔑 حل کامل یک جمله
    // ============================================================
    private suspend fun solveSentenceCompletely(sentenceId: Int, sentenceText: String): Boolean {
        // گارد اعتبارسنجی تعداد کلمات و ویرگول
        val validationError = validateSentence(sentenceId, sentenceText)
        if (validationError != null) {
            reportStuck("word_validation_error", validationError)
            return false
        }

        log("\n" + "█".repeat(50))
        log("🎯 حل کامل جمله $sentenceId")
        log("█".repeat(50))

        val parsed = displayParsedText(sentenceId, sentenceText)
        allWordsInAllSentences.addAll(parsed.words)

        val wordResults = solveWordByWord(sentenceId, sentenceText, parsed)
        val sentenceResults = solveSentenceBySentence(sentenceId, parsed)

        log("\n📖 حل کل جمله اصلی...")
        val fullSuccess = completeSentence(sentenceId)

        if (fullSuccess) {
            log("✅ کل جمله $sentenceId با موفقیت حل شد")
        } else {
            log("❌ حل کل جمله $sentenceId ناموفق بود")
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

    // ============================================================
    // 📋 تولید گزارش جامع و واحد (هم برای لاگ و هم برای ایمیل)
    // ============================================================
    fun generateReport(): String {
        val reportBuilder = StringBuilder()
        reportBuilder.append("📋 گزارش جامع و عملکرد ربات جملک\n")
        reportBuilder.append("=====================================\n")

        val pData = progress.load()
        val completedSentencesSize = pData.completedSentences.size
        val completedTitlesSize = pData.completedTitles.size

        reportBuilder.append("✅ جملات تکمیل شده در دیتابیس: $completedSentencesSize\n")
        reportBuilder.append("✅ عناوین تکمیل شده: $completedTitlesSize\n")
        reportBuilder.append("🔤 کلمات حل شده موفق: ${solvedWords.size}\n")
        reportBuilder.append("📖 جملات/عبارات پردازش شده: ${solvedSentences.size}\n")
        reportBuilder.append("📚 کل کلمات پردازش شده: ${allWordsInAllSentences.size}\n")
        reportBuilder.append("⚠️ تعداد کل خطاهای گزارش شده (Stuck): ${stuckReport.size}\n\n")


        if (stuckReport.isNotEmpty()) {
            reportBuilder.append("--- لیست خطاها، هشدارها و نواقص ساختاری ---\n")
            stuckReport.forEachIndexed { index, err ->
                reportBuilder.append("${index + 1}. $err\n")
            }
        } else {
            reportBuilder.append("🎉 هیچ خطایی در ساختار یا تعداد کلمات گزارش نشد!\n")
        }

        val finalReportText = reportBuilder.toString()
        log(finalReportText)
        return finalReportText
    }

    // ============================================================
    // متد اجرایی اصلی
    // ============================================================
    suspend fun run() {
        withContext(Dispatchers.IO) {
            log("🎮 ربات جملک")
            log("🎯 عناوین هدف: $targetTitleIds")
            log("🔬 حالت: حل کلمه به کلمه + جمله به جمله با اعتبارسنجی کلمات")
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
                    log("\n" + "█".repeat(50))
                    log("📚 عنوان $titleId: ${title.title ?: ""}")
                    log("█".repeat(50))

                    val currentData = progress.load()
                    if (currentData.completedTitles.contains(titleId)) {
                        log("✅ عنوان $titleId قبلاً تکمیل شده است.")
                        continue
                    }

                    val categories = getCategories(titleId)
                    log("📂 تعداد دسته‌بندی‌ها: ${categories.size}")

                    if (categories.isEmpty()) {
                        reportStuck("empty_categories", "برای عنوان $titleId (${title.title}) هیچ زیرعنوان یا شاخه‌ای یافت نشد.")
                        continue
                    }

                    var titleCompleted = true
                    var totalSentences = 0
                    var completedCount = 0

                    for (category in categories) {
                        val catId = category.id ?: continue
                        log("\n  📂 دسته: ${category.name ?: catId.toString()}")

                        val bases = getBases(catId)
                        if (bases.isEmpty()) {
                            reportStuck("empty_bases", "شاخه با شناسه $catId در عنوان $titleId فاقد پایه (Base) است.")
                            titleCompleted = false
                            continue
                        }

                        for (base in bases) {
                            val baseId = base.id ?: continue
                            log("\n    📁 پایه: ${base.name ?: baseId.toString()}")

                            val chapters = getChapters(baseId)
                            if (chapters.isEmpty()) {
                                reportStuck("empty_chapters", "پایه با شناسه $baseId فاقد فصل (Chapter) است.")
                                titleCompleted = false
                                continue
                            }

                            for (chapter in chapters) {
                                val chapterId = chapter.id ?: continue
                                log("\n      📖 فصل: ${chapter.name ?: chapterId.toString()}")

                                val sentences = getSentences(chapterId)
                                log("      تعداد جملات فصل: ${sentences.size}")

                                if (sentences.isEmpty()) {
                                    reportStuck(
                                        "empty_sentences",
                                        "فصل با شناسه $chapterId فاقد هرگونه جمله است."
                                    )
                                    titleCompleted = false
                                    continue
                                }

                                totalSentences += sentences.size

                                for (sentence in sentences) {
                                    val sentenceId = sentence.id ?: continue
                                    val text = getSentenceText(sentence)

                                    val success = solveSentenceCompletely(sentenceId, text)
                                    if (success) {
                                        completedCount++
                                    } else {
                                        titleCompleted = false
                                    }

                                    delay(delayMs)
                                }
                            }
                        }
                    }

                    log("\n📊 خلاصه عنوان $titleId: جملات $completedCount/$totalSentences")
                    if (titleCompleted && completedCount == totalSentences && totalSentences > 0) {
                        val dataToUpdate = progress.load()
                        if (!dataToUpdate.completedTitles.contains(titleId)) {
                            dataToUpdate.completedTitles.add(titleId)
                            progress.save(dataToUpdate)
                        }
                        log("\n🎉 عنوان $titleId کاملاً به پایان رسید!")
                    }
                }

                log("\n🎉 عملیات کاوش و حل به پایان رسید!")
                generateReport()

            } catch (e: Exception) {
                log("❌ خطای غیرمنتظره در ربات: ${e.message}")
                generateReport()
            }
        }
    }

    // ============================================================
    // توابع ارتباط با سرور (Retrofit API Wrappers)
    // ============================================================

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
                val isSuccess = body?.success ?: true
                if (isSuccess) {
                    val pData = progress.load()
                    if (!pData.completedSentences.contains(sentenceId)) {
                        pData.completedSentences.add(sentenceId)
                        progress.save(pData)
                    }
                }
                isSuccess
            } else {
                false
            }
        } catch (e: Exception) {
            if (e is com.google.gson.JsonSyntaxException || e is IllegalStateException) {
                val pData = progress.load()
                if (!pData.completedSentences.contains(sentenceId)) {
                    pData.completedSentences.add(sentenceId)
                    progress.save(pData)
                }
                true
            } else {
                false
            }
        }
    }

    // ============================================================
    // 📧 ارسال ایمیل با گزارش جامع و کامل
    // ============================================================
    suspend fun sendEmailReport(emailUser: String, emailPass: String, emailTo: String): Boolean? = withContext(Dispatchers.IO) {
        try {
            val config = EmailSender.EmailConfig(
                user = emailUser,
                password = emailPass,
                to = emailTo
            )

            // استفاده از گزارش جامع تولیدشده دقیقاً مشابه لاگ
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

    fun login(username: String, password: String): Boolean? {
        try {
            log("🔑 در حال ارسال درخواست ورود برای کاربر: $username")
            val response = api.login(username, password)
                .execute()
            if (response.isSuccessful && response.body()?.success == true) {
                val newToken = response.body()?.result?.token
                if (!newToken.isNullOrEmpty()) {
                    token = newToken
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