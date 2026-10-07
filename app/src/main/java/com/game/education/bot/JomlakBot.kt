package com.game.education.bot

import android.content.Context
import android.util.Log
import com.game.education.api.ApiInterface
import com.game.education.api.RetrofitClient
import com.game.education.api.ServerData
import kotlinx.coroutines.delay

class JomlakBot(
    private val context: Context,
    private val token: String,
    private val onLog: (String) -> Unit
) {
    private val api: ApiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface::class.java)
    private val progressManager = ProgressManager(context)
    private val progress = progressManager.load()

    private val solvedWords = mutableListOf<Pair<Int, String>>()
    private val solvedSentences = mutableListOf<Pair<Int, String>>()
    private val fullLog = StringBuilder()

    private fun log(message: String) {
        fullLog.appendLine(message)
        onLog(message)
    }

    suspend fun run(): Boolean {
        log("🚀 شروع کاوش...")
        log("🎯 عناوین هدف: 1-4")
        log("=".repeat(60))

        return try {
            val titles = getTitles()
            log("📚 تعداد کل عناوین: " + titles.size)

            if (titles.isEmpty()) {
                log("⚠️ هشدار: هیچ عنوانی دریافت نشد!")
                return false
            }

            val targetTitles = titles.filter { it.id in listOf(1, 2, 3, 4) }
            log("🎯 عناوین هدف: " + targetTitles.size)

            for (title in targetTitles) {
                log("")
                log("=".repeat(60))
                log("📚 عنوان " + title.id + ": " + title.title)
                log("=".repeat(60))

                if (progress.completedTitles.contains(title.id)) {
                    log("✅ قبلاً تکمیل شده")
                    continue
                }

                processTitle(title)
                progressManager.save(progress)
            }

            log("")
            log("🎉 کاوش کامل شد!")
            printReport()
            true
        } catch (e: Exception) {
            log("❌ خطای کلی: " + e.message)
            Log.e("JomlakBot", "Error", e)
            false
        }
    }

    private suspend fun processTitle(title: ServerData.Title) {
        val categories = getCategories(title.id)
        log("📂 دسته‌بندی‌ها: " + categories.size)

        var allCompleted = true

        for (category in categories) {
            val bases = getBases(category.id)
            for (base in bases) {
                val chapters = getChapters(base.id)
                for (chapter in chapters) {
                    val sentences = getSentences(chapter.id)
                    log("📖 فصل " + (chapter.name ?: chapter.id) + ": " + sentences.size + " جمله")

                    for (sentence in sentences) {
                        val success = processSentence(sentence)
                        if (!success) allCompleted = false
                        delay(100)
                    }
                }
            }
        }

        if (allCompleted) {
            progress.completedTitles.add(title.id)
            progressManager.save(progress)
            log("🎉 عنوان " + title.id + " کامل شد!")
        }
    }

    private suspend fun processSentence(sentence: ServerData.Sentence): Boolean {
        log("")
        log("=".repeat(80))
        log("📝 شناسه جمله: " + sentence.id)
        log("=".repeat(80))
        log("📖 متن کامل: " + sentence.title)
        log("-".repeat(80))

        val parsed = PersianParser.parse(sentence.title)

        log("📚 تعداد جملات/عبارات: " + parsed.sentenceCount)
        log("-".repeat(80))

        parsed.sentences.forEach { sent ->
            log("")
            log("  " + sent.index + ". جمله/عبارت:")
            log("     📖 " + sent.text)
            sent.separator?.let { log("     🔚 جداکننده: " + it) }
            log("     🔤 کلمات (" + sent.words.size + "):")
            sent.words.forEachIndexed { i, w ->
                log("        " + (i + 1) + ". " + w)
            }
        }

        log("")
        log("🔤 تمام کلمات یکتا (" + parsed.uniqueWords.size + "):")
        parsed.uniqueWords.forEachIndexed { i, w ->
            log("     " + (i + 1) + ". " + w)
        }

        // حل کلمه به کلمه
        log("")
        log("🔤 شروع حل کلمه به کلمه...")
        for (word in parsed.uniqueWords) {
            val key = sentence.id.toString() + "_word_" + word
            if (progress.completedWordTests[key] == true) {
                log("  ⏭️ قبلاً حل شده: " + word)
                continue
            }
            log("  🔤 حل: " + word)
            val success = completeSentence(sentence.id)
            if (success) {
                progress.completedWordTests[key] = true
                solvedWords.add(Pair(sentence.id, word))
                log("  ✅ موفق")
            } else {
                log("  ❌ ناموفق")
            }
            delay(80)
        }

        // حل جمله به جمله
        log("")
        log("📖 شروع حل جمله به جمله...")
        for (sent in parsed.sentences) {
            val key = sentence.id.toString() + "_sentence_" + sent.text
            if (progress.completedSentenceTests[key] == true) {
                log("  ⏭️ قبلاً حل شده")
                continue
            }
            log("  📖 حل: " + sent.text)
            val success = completeSentence(sentence.id)
            if (success) {
                progress.completedSentenceTests[key] = true
                solvedSentences.add(Pair(sentence.id, sent.text))
                log("  ✅ موفق")
            } else {
                log("  ❌ ناموفق")
            }
            delay(80)
        }

        // حل کل جمله
        log("")
        log("📖 حل کل جمله اصلی...")
        val fullSuccess = completeSentence(sentence.id)
        if (fullSuccess) {
            if (!progress.completedSentences.contains(sentence.id)) {
                progress.completedSentences.add(sentence.id)
            }
            log("✅ کل جمله حل شد")
        } else {
            log("❌ کل جمله حل نشد")
        }

        progressManager.save(progress)
        return fullSuccess
    }

    private suspend fun getTitles(): List<ServerData.Title> {
        return try {
            api.titles("fa").execute().body()?.result ?: emptyList()
        } catch (e: Exception) {
            log("❌ خطا در دریافت عناوین: " + e.message)
            emptyList()
        }
    }

    private suspend fun getCategories(titleId: Int): List<ServerData.Category> {
        return try {
            api.categories(titleId, "fa").execute().body()?.result ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    private suspend fun getBases(categoryId: Int): List<ServerData.Base> {
        return try {
            api.bases(categoryId, "fa").execute().body()?.result ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    private suspend fun getChapters(baseId: Int): List<ServerData.Chapter> {
        return try {
            api.chapters(baseId, "fa").execute().body()?.result ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    private suspend fun getSentences(chapterId: Int): List<ServerData.Sentence> {
        return try {
            val body = api.sentences(chapterId, "fa").execute().body()
            body?.items ?: body?.result ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    private suspend fun completeSentence(sentenceId: Int): Boolean {
        return try {
            val body = mapOf<String, Any>("sentence_id" to sentenceId)
            val response = api.sentences_complete(token, body).execute()
            response.isSuccessful && (response.body()?.success == true)
        } catch (e: Exception) { false }
    }

    private fun printReport() {
        log("")
        log("=".repeat(60))
        log("📋 گزارش نهایی")
        log("=".repeat(60))
        log("✅ جملات تکمیل شده: " + progress.completedSentences.size)
        log("✅ عناوین تکمیل شده: " + progress.completedTitles.size)
        log("🔤 کلمات حل شده: " + solvedWords.size)
        log("📖 جملات/عبارات حل شده: " + solvedSentences.size)
        log("=".repeat(60))
    }

    suspend fun sendEmailReport(
        emailUser: String,
        emailPass: String,
        emailTo: String
    ): Boolean {
        log("📧 در حال ارسال گزارش...")

        val config = EmailSender.EmailConfig(
            user = emailUser,
            password = emailPass,
            to = emailTo
        )

        val subject = "گزارش ربات جملک - " + java.util.Date()

        val wordsHtml = buildString {
            append("<h3>🔤 کلمات حل شده (" + solvedWords.size + ")</h3><ul>")
            solvedWords.take(500).forEach { pair ->
                append("<li>شناسه " + pair.first + ": <strong>" + pair.second + "</strong></li>")
            }
            append("</ul>")
        }

        val sentencesHtml = buildString {
            append("<h3>📖 جملات/عبارات حل شده (" + solvedSentences.size + ")</h3><ul>")
            solvedSentences.forEach { pair ->
                append("<li>شناسه " + pair.first + ": <strong>" + pair.second + "</strong></li>")
            }
            append("</ul>")
        }

        val htmlBody = """
            <html dir="rtl">
            <head><meta charset="UTF-8"></head>
            <body style="font-family: Tahoma; direction: rtl; padding: 20px;">
                <div style="background: #4CAF50; color: white; padding: 20px; border-radius: 8px;">
                    <h1>🤖 گزارش ربات جملک</h1>
                    <p>تاریخ: ${java.util.Date()}</p>
                </div>
                <div style="background: #e3f2fd; padding: 15px; border-radius: 8px; margin: 20px 0;">
                    <h3>📊 خلاصه</h3>
                    <p>✅ جملات: ${progress.completedSentences.size}</p>
                    <p>✅ عناوین: ${progress.completedTitles.size}</p>
                    <p>🔤 کلمات: ${solvedWords.size}</p>
                    <p>📖 عبارات: ${solvedSentences.size}</p>
                </div>
                <div style="background: #f5f5f5; padding: 15px; border-radius: 8px;">$wordsHtml</div>
                <div style="background: #f5f5f5; padding: 15px; border-radius: 8px;">$sentencesHtml</div>
                <pre style="background: #1e1e1e; color: #d4d4d4; padding: 15px; border-radius: 8px;">${fullLog}</pre>
            </body>
            </html>
        """.trimIndent()

        return EmailSender.sendReport(config, subject, htmlBody, fullLog.toString()).fold(
            onSuccess = {
                log("✅ ایمیل ارسال شد: " + it)
                true
            },
            onFailure = {
                log("❌ ارسال ایمیل ناموفق: " + it.message)
                false
            }
        )
    }
}