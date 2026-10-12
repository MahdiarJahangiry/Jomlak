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
     * اجرای ربات با داده‌های ارسال شده از Game.java
     * (همان قبلی) -> خروجی Unit
     */
    suspend fun runOnGameData(
        ids: List<Int>,
        questions: List<String>
    ) = withContext(Dispatchers.IO) {
        fullLog.setLength(0)
        solvedWords.clear()
        solvedSentences.clear()

        log("🚀 شروع فرآیند عیب‌یابی و اجرای خودکار ربات ادمین...")
        log("تعداد مراحل/جملات دریافتی از بازی: ${questions.size}")

        if (questions.isEmpty()) {
            log("⚠️ هیچ جمله‌ای برای تست دریافت نشد!")
            return@withContext
        }

        for (index in questions.indices) {
            val stageId = if (index < ids.size) ids[index] else index + 1
            val rawQuestion = questions[index]

            log("--------------------------------------------------")
            log("📌 در حال بررسی مرحله ${index + 1} (شناسه: $stageId)")
            log("متن مرحله: $rawQuestion")

            val parsedResult = PersianParser.parse(rawQuestion)
            val words = parsedResult.allWords

            if (words.isEmpty()) {
                log("❌ خطا در مرحله ${index + 1}: هیچ کلمه‌ای از جمله استخراج نشد (مشکل در فرمت متن).")
                continue
            }

            var stageSuccess = true

            for ((wordIndex, word) in words.withIndex()) {
                if (word.isBlank()) {
                    log("⚠️ اخطار در مرحله ${index + 1}: کلمه شماره ${wordIndex + 1} خالی است!")
                    stageSuccess = false
                } else {
                    log("  ✅ کلمه حل شد: [$word]")
                    solvedWords.add(word)
                }
            }

            if (stageSuccess) {
                solvedSentences.add(rawQuestion)
                log("🎉 مرحله ${index + 1} با موفقیت کامل پاس شد.")
            } else {
                log("⛔ مرحله ${index + 1} با وجود خطا به پایان رسید.")
            }
        }

        log("==================================================")
        log("🏁 پایان فرآیند تست. کل کلمات حل شده: ${solvedWords.size} | کل جملات کامل: ${solvedSentences.size}")
    }

    /**
     * نسخه Boolean دار بر اساس همان runOnGameData
     *
     * تعریف "موفقیت":
     * - حداقل یک سوال وجود داشته باشد
     * - و تعداد جملات کامل شده == تعداد سوالات
     * - و هیچ Exception رخ ندهد
     */
    suspend fun runOnGameDataWithResult(
        ids: List<Int>,
        questions: List<String>
    ): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            runOnGameData(ids, questions)

            val success = questions.isNotEmpty() && solvedSentences.size == questions.size
            if (success) {
                log("✅ نتیجه نهایی: همه مراحل با موفقیت پاس شدند.")
            } else {
                log("❌ نتیجه نهایی: حداقل یک مرحله ناموفق بود یا ورودی خالی بود.")
            }
            success
        } catch (e: Exception) {
            log("❌ استثنا در اجرای ربات: ${e.message}")
            false
        }
    }

    // sendEmailReport همان قبلی‌ات، فقط نوع‌ها را String فرض کردم برای مثال
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

            val subject = "گزارش ربات جملک (ادمین) - ${Date()}"

            val htmlBody = buildString {
                append("<div dir='rtl' style='font-family:Tahoma,sans-serif;'>")
                append("<h2>گزارش تست خودکار بازی جملک</h2>")
                append("<p><b>تعداد کلمات حل شده:</b> ${solvedWords.size}</p>")
                append("<p><b>تعداد جملات کامل شده:</b> ${solvedSentences.size}</p>")
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
                log("✅ ایمیل گزارش با موفقیت ارسال شد.")
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
