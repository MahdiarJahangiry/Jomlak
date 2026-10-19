package com.game.education.bot

import android.content.Context
import com.game.education.api.ApiInterface
import com.game.education.api.RetrofitClient
import com.game.education.api.ServerData
import com.google.gson.annotations.SerializedName
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
            log("📚 تعداد کل عناوین دریافت شده: ${titles.size}")

            if (titles.isEmpty()) {
                log("⚠️ هشدار: هیچ عنوانی از سرور دریافت نشد!")
                return@withContext false
            }

            // پردازش عناوین (بدون محدود کردن سخت‌گیرانه به 1..4 در صورت تفاوت آیدی‌ها)
            for (title in titles) {
                log("\n📚 بررسی عنوان ${title.id}: ${title.title ?: title.name}")
                if (progress.completedTitles.contains(title.id)) {
                    log("✅ این عنوان قبلاً تکمیل شده است.")
                    continue
                }

                processTitle(title)
                progressManager.save(progress)
            }

            log("\n🎉 عملیات بررسی و کاوش کامل شد!")
            printReport()
            true
        } catch (e: Exception) {
            log("❌ خطای کلی در اجرای ربات: ${e.message}")
            false
        }
    }

    private suspend fun processTitle(title: ServerData.Title) {
        val categories = getCategories(title.id)
        log("📂 دسته‌بندی‌های عنوان [${title.id}]: ${categories.size}")

        if (categories.isEmpty()) {
            log("⚠️ هیچ دسته‌ای برای عنوان ${title.id} بازگردانده نشد.")
            return
        }

        for (cat in categories) {
            log("  📁 دسته [${cat.id}]: ${cat.name ?: cat.title}")
            val bases = getBases(cat.id)
            log("    📂 پایه‌های دسته [${cat.id}]: ${bases.size}")

            for (base in bases) {
                log("    📂 پایه [${base.id}]: ${base.name ?: base.title}")
                val chapters = getChapters(base.id)
                log("      📖 فصل‌های پایه [${base.id}]: ${chapters.size}")

                for (chapter in chapters) {
                    log("      📖 فصل [${chapter.id}]: ${chapter.name ?: chapter.title}")
                    val sentences = getSentences(chapter.id)

                    if (sentences.isEmpty()) {
                        log("        ⚠️ سوالی در فصل [${chapter.id}] یافت نشد.")
                    } else {
                        log("        📥 شروع حل ${sentences.size} سوال برای فصل [${chapter.id}]...")
                    }

                    for (sentence in sentences) {
                        completeSentence(sentence.id)
                        delay(80)
                    }
                }
            }
        }

        progress.completedTitles.add(title.id)
        progressManager.save(progress)
    }
    // --- توابع دریافت اطلاعات با سازگاری دوگانه ---

    private fun getTitles(): List<ServerData.Title> {
        return try {
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

    private fun getCategories(titleId: Int): List<ServerData.Category> {
        return try {
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

    private fun getBases(categoryId: Int): List<ServerData.Base> {
        return try {
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

    private fun getChapters(baseId: Int): List<ServerData.Chapter> {
        return try {
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

    private fun getSentences(chapterId: Int): List<ServerData.Sentence> {
        return try {
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


    private suspend fun completeSentence(sentenceId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            // اطمینان از قرار گرفتن پیشوند Bearer برای هدر Authorization
            val authToken = if (token?.startsWith("Bearer ") == true) token else "Bearer $token"

            val response = api.sentences_complete(
                authToken,
                mapOf("sentence_id" to sentenceId)
            ).execute()

            if (response.isSuccessful) {
                val body = response.body()
                // خواندن مستقیم فیلد success به جای isSuccess
                val isSuccess = body?.success ?: true
                if (isSuccess) {
                    log("  ✅ جمله $sentenceId با موفقیت حل شد.")
                    solvedSentences.add(Pair(sentenceId, "OK"))
                    progress.completedSentences.add(sentenceId)
                }
                isSuccess
            } else {
                val errorStr = response.errorBody()?.string() ?: response.message()
                log("⚠️ خطای سرور در حل جمله $sentenceId: کد ${response.code()} - $errorStr")
                false
            }
        } catch (e: Exception) {
            if (e is com.google.gson.JsonSyntaxException || e is IllegalStateException) {
                log("ℹ️ جمله $sentenceId ارسال شد (پاسخ سرور متنی بود).")
                solvedSentences.add(Pair(sentenceId, "OK"))
                progress.completedSentences.add(sentenceId)
                true
            } else {
                log("❌ استثنا در تکمیل جمله $sentenceId: ${e.localizedMessage}")
                false
            }
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
            //log("❌ خطای کلی در اجرای ربات: ${e.message}")
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
