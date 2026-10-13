package com.game.education.bot

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class JomlakBot(
    private val onLog: (String) -> Unit
) {

    private val fullLog = StringBuilder()
    private val solvedWords = mutableListOf<String>()
    private val solvedSentences = mutableListOf<String>()

    private fun log(message: String) {
        val entry = "[${Date()}] $message"
        fullLog.append(entry).append("\n")
        onLog(message)
    }

    /**
     * اجرای تست جملات دریافتی و بازگرداندن خروجی بافرمت Boolean
     */
    suspend fun runOnGameDataWithResult(
        ids: List<Int>,
        questions: List<String>
    ): Boolean = withContext(Dispatchers.IO) {
        fullLog.setLength(0)
        solvedWords.clear()
        solvedSentences.clear()

        log("🚀 شروع فرآیند تست خودکار و تحلیل کلمات و جملات...")
        log("تعداد کل مراحل/جملات دریافتی: ${questions.size}")

        if (questions.isEmpty()) {
            log("⚠️ هیچ جمله‌ای برای تست در دسترس نیست! (لیست ارسالی خالی است)")
            return@withContext false
        }

        var totalPassed = true

        for (index in questions.indices) {
            val stageId = if (index < ids.size) ids[index] else index + 1
            val rawQuestion = questions[index].trim()

            log("--------------------------------------------------")
            log("📌 مرحله ${index + 1} (شناسه: $stageId)")
            log("متن خام: \"$rawQuestion\"")

            // جملات ممکن است با ویرگول/کاما چند بخشی باشند
            val subSentences = rawQuestion.split(",", "،").map { it.trim() }.filter { it.isNotEmpty() }

            var stageSuccess = true

            for ((sentenceIdx, subSentence) in subSentences.withIndex()) {
                // استخراج کلمات تفکیک شده با فاصله
                val words = subSentence.split("\\s+".toRegex()).filter { it.isNotBlank() }

                if (words.isEmpty()) {
                    log("  ❌ بخش ${sentenceIdx + 1}: هیچ کلمه‌ای استخراج نشد!")
                    stageSuccess = false
                    continue
                }

                log("  🔹 بخش ${sentenceIdx + 1} (${words.size} کلمه):")
                for (w in words) {
                    val cleanWord = w.replace("[.,،!؟?]".toRegex(), "").trim()
                    if (cleanWord.isNotEmpty()) {
                        solvedWords.add(cleanWord)
                        log("     ✅ کلمه تایید شد: [$cleanWord]")
                    }
                }
            }

            if (stageSuccess && subSentences.isNotEmpty()) {
                solvedSentences.add(rawQuestion)
                log("🎉 مرحله ${index + 1} با موفقیت تایید و تست شد.")
            } else {
                totalPassed = false
                log("⛔ مرحله ${index + 1} دارای نقص در ساختار بود.")
            }
        }

        log("==================================================")
        log("🏁 پایان فرآیند تست. کل کلمات استخراج شده: ${solvedWords.size} | مراحل کامل: ${solvedSentences.size}/${questions.size}")

        return@withContext totalPassed && solvedSentences.isNotEmpty()
    }

    suspend fun sendEmailReport(
        emailUser: String,
        emailPass: String,
        emailTo: String
    ): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            log("📧 در حال آماده‌سازی و ارسال گزارش به $emailTo...")

            val config = EmailSender.EmailConfig(
                user = emailUser,
                password = emailPass,
                to = emailTo,
                host = "smtp.gmail.com",
                port = 587
            )

            val subject = "گزارش تست خودکار ربات جملک - ${Date()}"

            val htmlBody = buildString {
                append("<div dir='rtl' style='font-family:Tahoma,sans-serif;'>")
                append("<h2>گزارش تست ربات ادمین</h2>")
                append("<p><b>تعداد کلمات موفق:</b> ${solvedWords.size}</p>")
                append("<p><b>تعداد مراحل کامل شده:</b> ${solvedSentences.size}</p>")
                append("<hr/>")
                append("<h3>لاگ کامل عملیات:</h3>")
                append("<pre style='background:#f4f4f4;padding:10px;border-radius:5px;direction:ltr;text-align:left;'>")
                append(fullLog.toString())
                append("</pre>")
                append("</div>")
            }

            val result: Result<String> = EmailSender.sendReport(
                config = config,
                subject = subject,
                htmlBody = htmlBody,
                plainBody = fullLog.toString()
            )

            if (result.isSuccess) {
                log("✅ گزارش با موفقیت ایمیل شد.")
                true
            } else {
                val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "خطای ناشناخته"
                log("❌ خطا در ارسال ایمیل: $errorMsg")
                false
            }
        } catch (e: Exception) {
            log("❌ استثنا در زمان ارسال ایمیل: ${e.message}")
            false
        }
    }
}
