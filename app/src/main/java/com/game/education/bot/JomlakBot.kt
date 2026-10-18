package com.game.education.bot

import android.content.Context
import android.util.Log
import com.game.education.api.ApiInterface
import com.game.education.api.RetrofitClient
import com.game.education.api.ServerData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


class JomlakBot(
    private val context: Context,
    private var token: String? = null,
    private val onLog: (String) -> Unit
) {
    private val api: ApiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface::class.java)
    private val progressManager = ProgressManager(context)
    private val progress = progressManager.load()

    private val solvedSentences = mutableListOf<Pair<Int, String>>()
    private val fullLog = StringBuilder()

    private fun log(message: String) {
        fullLog.appendLine(message)
        onLog(message)
    }

    /**
     * ورود و ذخیره توکن
     */
    suspend fun login(username: String, pass: String): Boolean = withContext(Dispatchers.IO) {
        try {
            log("🔑 در حال ورود ربات...")
            val call = api.login(username, pass)
            val response = call.execute()
            if (response.isSuccessful && response.body()?.success == true) {
                token = response.body()?.result?.token
                log("✅ ورود موفقیت‌آمیز بود!")
                true
            } else {
                val err = response.body()?.error ?: response.message()
                log("❌ خطا در ورود: $err")
                false
            }
        } catch (e: Exception) {
            log("❌ خطای شبکه در لاگین: ${e.localizedMessage}")
            false
        }
    }

    /**
     * اجرای چرخه اصلی ربات
     */
    suspend fun run(): Boolean = withContext(Dispatchers.IO) {
        val currentToken = token
        if (currentToken.isNullOrBlank()) {
            log("❌ خطا: توکن احراز هویت خالی است. لطفاً ابتدا وارد شوید.")
            return@withContext false
        }

        try {
            log("🚀 آغاز کاوش سوالات...")
            val titles = getTitles()
            log("📚 تعداد کل عناوین: ${titles.size}")

            if (titles.isEmpty()) {
                log("⚠️ هشدار: هیچ عنوانی دریافت نشد!")
                return@withContext false
            }

            val targetTitles = titles.filter { it.id in listOf(1, 2, 3, 4) }
            log("🎯 عناوین هدف: ${targetTitles.size}")

            for (title in targetTitles) {
                log("\n📚 عنوان ${title.id}: ${title.title ?: title.name}")
                if (progress.completedTitles.contains(title.id)) {
                    log("✅ قبلاً تکمیل شده")
                    continue
                }

                processTitle(title)
                progressManager.save(progress)
            }

            log("\n🎉 کاوش کامل شد!")
            printReport()
            true
        } catch (e: Exception) {
            log("❌ خطای کلی: ${e.message}")
            Log.e("JomlakBot", "Error", e)
            false
        }
    }

     suspend fun sendEmailReport(emailUser: String, emailPass: String, emailTo: String): Boolean {
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
            false
        }
    }


    private suspend fun processTitle(title: ServerData.Title) {
        val categories = getCategories(title.id)
        log("📂 دسته‌بندی‌ها: ${categories.size}")

        for (cat in categories) {
            log("  📁 دسته: ${cat.name ?: cat.title}")
            val bases = getBases(cat.id)

            for (base in bases) {
                log("    📂 پایه: ${base.name ?: base.title}")
                val chapters = getChapters(base.id)

                for (chapter in chapters) {
                    log("      📖 فصل: ${chapter.name ?: chapter.title}")
                    val sentences = getSentences(chapter.id)

                    for (sentence in sentences) {
                        processSentence(sentence)
                        delay(100)
                    }
                }
            }
        }

        progress.completedTitles.add(title.id)
        progressManager.save(progress)
    }

    private suspend fun processSentence(sentence: ServerData.Sentence): Boolean {
        if (progress.completedSentences.contains(sentence.id)) {
            return true
        }

        val text = sentence.title ?: ""
        log("        🧩 سوال: $text (ID: ${sentence.id})")

        val success = completeSentence(sentence.id)
        if (success) {
            log("        ✅ سوال [${sentence.id}] حل شد.")
            solvedSentences.add(Pair(sentence.id, text))
            progress.completedSentences.add(sentence.id)
            progressManager.save(progress)
        } else {
            log("        ⚠️ خطا در ثبت حل سوال [${sentence.id}].")
        }

        return success
    }

    // --- توابع دریافت اطلاعات هماهنگ با ApiInterface ---

      private fun getTitles(): List<ServerData.Title> {
        return try {
            val response = api.titles("fa").execute()
            if (response.isSuccessful) {
                val body = response.body()
                when (body) {
                    is ServerData.TitleResponse -> body.result ?: emptyList()
                    else -> emptyList()
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getCategories(titleId: Int): List<ServerData.Category> {
        return try {
            val response = api.categories(titleId, "fa").execute()
            if (response.isSuccessful) {
                val body = response.body()
                when (body) {
                    is ServerData.CategoryResponse -> body.result ?: emptyList()
                    else -> emptyList()
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getBases(categoryId: Int): List<ServerData.Base> {
        return try {
            val response = api.bases(categoryId, "fa").execute()
            if (response.isSuccessful) {
                val body = response.body()
                when (body) {
                    is ServerData.BaseResponse -> body.result ?: emptyList()
                    else -> emptyList()
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getChapters(baseId: Int): List<ServerData.Chapter> {
        return try {
            val response = api.chapters(baseId, "fa").execute()
            if (response.isSuccessful) {
                val body = response.body()
                when (body) {
                    is ServerData.ChapterResponse -> body.result ?: emptyList()
                    else -> emptyList()
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getSentences(chapterId: Int): List<ServerData.Sentence> {
        return try {
            val response = api.sentences(chapterId, "fa").execute()
            if (response.isSuccessful) {
                val rawBody: Any? = response.body()
                when (rawBody) {
                    is ServerData.SentenceResponse -> {
                        rawBody.result ?: rawBody.items ?: emptyList()
                    }
                    is ServerData.levels -> {
                        // در صورتی که امضای ApiInterface روی ServerData.levels تنظیم باشد
                        rawBody.items?.map { item ->
                            ServerData.Sentence().apply {
                                this.id = item.id
                                this.title = item.title
                                this.chapterId = chapterId
                            }
                        } ?: emptyList()
                    }
                    else -> emptyList()
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun completeSentence(sentenceId: Int): Boolean {
        return try {
            val currentToken = token ?: return false
            val authHeader = if (currentToken.startsWith("Bearer ")) currentToken else "Bearer $currentToken"
            val body = mapOf<String, Any>("sentence_id" to sentenceId)
            val res = api.sentences_complete(authHeader, body).execute()
            res.isSuccessful && (res.body()?.success == true)
        } catch (e: Exception) {
            false
        }
    }

    private fun printReport() {
        log("\n" + "=".repeat(40))
        log("📊 گزارش عملکرد ربات:")
        log("  - جملات حل‌شده در این مرحله: ${solvedSentences.size}")
        log("  - کل جملات تکمیل‌شده: ${progress.completedSentences.size}")
        log("  - عنوان‌های تکمیل‌شده: ${progress.completedTitles.size}")
        log("=".repeat(40))
    }
}
