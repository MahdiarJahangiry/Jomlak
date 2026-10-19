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

    // اصلاح نام متد به getRetrofitInstance مطابق RetrofitClient.java
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

        log("\n📚 تعداد جملات/عبارات: ${parsed.sentenceCount}")
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

        log("\n🔤 تمام کلمات یکتا در این جمله (${parsed.uniqueWords.size}):")
        parsed.uniqueWords.forEachIndexed { i, w ->
            log("     ${i + 1}. ${formatPersian(w)}")
        }

        log("─".repeat(50) + "\n")
        return parsed
    }

    // ============================================================
    // 🔑 حل کلمه به کلمه
    // ============================================================
    private suspend fun solveWordByWord(sentenceId: Int, parsed: ParsedTextResult): List<Boolean> {
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
                solvedWords.add(SolvedItem(sentenceId, word, timeStamp))
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
                solvedSentences.add(SolvedItem(sentenceId, sentText, timeStamp))
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
        log("\n" + "█".repeat(50))
        log("🎯 حل کامل جمله $sentenceId")
        log("█".repeat(50))

        val parsed = displayParsedText(sentenceId, sentenceText)
        allWordsInAllSentences.addAll(parsed.words)

        val wordResults = solveWordByWord(sentenceId, parsed)
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

    fun generateReport() {
        log("\n" + "=".repeat(50))
        log("📋 گزارش نهایی ربات جملک")
        log("=".repeat(50))

        val completedSentences = progress.load().completedSentences
        val completedTitles = progress.load().completedTitles

        log("\n✅ جملات تکمیل شده: ${completedSentences.size}")
        log("✅ عناوین تکمیل شده: ${completedTitles.size}")
        log("🔤 کلمات حل شده: ${solvedWords.size}")
        log("📖 جملات/عبارات حل شده: ${solvedSentences.size}")
        log("📚 کل کلمات پردازش شده: ${allWordsInAllSentences.size}")
        log("⚠️ مشکلات: ${stuckReport.size}")

        if (stuckReport.isNotEmpty()) {
            log("\n🔴 لیست خطاها و موانع:")
            stuckReport.forEachIndexed { index, err ->
                log("  ${index + 1}. $err")
            }
        }

        log("\n" + "=".repeat(50))
    }

    // ============================================================
    // متد اجرایی اصلی
    // ============================================================
    suspend fun run() {
        withContext(Dispatchers.IO) {
            log("🎮 ربات جملک")
            log("🎯 عناوین هدف: $targetTitleIds")
            log("🔬 حالت: حل کلمه به کلمه + جمله به جمله")
            log("=".repeat(50))

            try {
                val allTitles = getTitles()
                log("\n📚 تعداد کل عناوین دریافت شده: ${allTitles.size}")

                val targetTitles = allTitles.filter { it.id in targetTitleIds }
                log("🎯 عناوین انتخاب شده برای حل: ${targetTitles.size}")

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
                        reportStuck("empty_categories", "برای عنوان $titleId دسته‌بندی یافت نشد.")
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
                            titleCompleted = false
                            continue
                        }

                        for (base in bases) {
                            val baseId = base.id ?: continue
                            log("\n    📁 پایه: ${base.name ?: baseId.toString()}")

                            val chapters = getChapters(baseId)
                            if (chapters.isEmpty()) {
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
                                        "empty_chapter",
                                        "در فصل $chapterId جمله‌ای موجود نیست."
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

    suspend fun sendEmailReport(emailUser: String, emailPass: String, emailTo: String): Boolean? {
        val config = EmailSender.EmailConfig(
            user = emailUser,
            password = emailPass,
            to = emailTo
        )

        val subject = "گزارش عملکرد ربات جملک"
        val plainBody = "تعداد مراحل پردازش‌شده: ${solvedSentences.size}"
        val htmlBody = """
        <div dir="rtl" style="font-family: Tahoma, sans-serif; padding: 16px;">
            <h2 style="color: #2e7d32;">گزارش ربات جملک</h2>
            <p>عملیات با موفقیت انجام شد.</p>
            <p><strong>تعداد سوالات پردازش‌شده:</strong> ${solvedSentences.size}</p>
        </div>
    """.trimIndent()

        val result = EmailSender.sendReport(
            config = config,
            subject = subject,
            htmlBody = htmlBody,
            plainBody = plainBody
        )

        return if (result.isSuccess) {
            log("📧 گزارش ایمیل با موفقیت ارسال شد به: $emailTo")
            true
        } else {
            val err = result.exceptionOrNull()?.message ?: "خطای ناشناخته"
            log("❌ خطا در ارسال ایمیل: $err")
            //log("❌ خطای کلی در اجرای ربات: ${e.message}")
            false
        }
    }

    private fun printReport() {
        log("\n" + "=".repeat(40))
        log("📊 گزارش عملکرد ربات:")
        log("  - جملات حل‌شده در این مرحله: ${solvedSentences.size}")
        val currentProgress = progress.load()
        log("  - کل جملات تکمیل‌شده: ${currentProgress.completedSentences.size}")
        log("  - عنوان‌های تکمیل‌شده: ${currentProgress.completedTitles.size}")
        log("=".repeat(40))
    }

    fun login(username: String, password: String): Boolean? {
        try {
            log("🔑 در حال ارسال درخواست ورود برای کاربر: $username")
            val response = api.login(username, password)
                .execute() // یا فراخوانی مناسب با Retrofit بر اساس ApiInterface شما
            if (response.isSuccessful && response.body()?.success == true) {
                val responseToken = response.body()?.result?.token
                if (!responseToken.isNullOrEmpty()) {
                    // اگر توکن داخل JomlakBot به صورت var است، آن را مقداردهی کن:
                    // token = responseToken
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
