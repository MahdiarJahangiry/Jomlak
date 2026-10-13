package com.game.education.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.game.education.App
import com.game.education.R
import com.game.education.api.ApiInterface
import com.game.education.api.RetrofitClient
import com.game.education.bot.JomlakBot
import com.game.education.bot.LogAdapter
import com.game.education.model.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BotActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnStart: Button
    private lateinit var btnClear: Button
    private lateinit var btnSendEmail: Button
    private lateinit var etEmailUser: EditText
    private lateinit var etEmailPass: EditText
    private lateinit var etEmailTo: EditText
    private var progressBar: ProgressBar? = null
    private lateinit var recyclerLogs: RecyclerView

    private lateinit var prefs: Prefs
    private lateinit var logAdapter: LogAdapter
    private var bot: JomlakBot? = null
    private var isRunning: Boolean = false

    // داده‌های مراحل
    private var receivedIds = arrayListOf<Int>()
    private var receivedQuestions = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bot)

        prefs = Prefs(this)
        initViews()
        setupRecycler()
        setupListeners()

        // استخراج داده‌های فرستاده‌شده از Game.java
        extractIntentData()

        checkAdminSession()
    }

    private fun initViews() {
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnStart = findViewById(R.id.btnStart)
        btnClear = findViewById(R.id.btnClear)
        btnSendEmail = findViewById(R.id.btnSendEmail)
        etEmailUser = findViewById(R.id.etEmailUser)
        etEmailPass = findViewById(R.id.etEmailPass)
        etEmailTo = findViewById(R.id.etEmailTo)
        progressBar = findViewById(R.id.progressBar)
        recyclerLogs = findViewById(R.id.recyclerLogs)
    }

    private fun setupRecycler() {
        logAdapter = LogAdapter()
        recyclerLogs.layoutManager = LinearLayoutManager(this)
        recyclerLogs.adapter = logAdapter
    }

    private fun extractIntentData() {
        intent.getIntegerArrayListExtra("EXTRA_IDS")?.let {
            receivedIds = it
        }
        intent.getStringArrayListExtra("EXTRA_QUESTIONS")?.let {
            receivedQuestions = it
        }

        if (receivedQuestions.isNotEmpty()) {
            appendLog("📥 تعداد ${receivedQuestions.size} سوال از جریان بازی دریافت گردید.")
        }
    }

    private fun checkAdminSession() {
        val currentUsername = prefs.GetUsername()
        if (currentUsername == "admin") {
            btnStart.visibility = View.VISIBLE
            btnStart.isEnabled = true
            appendLog("ادمین شناسایی شد. پنل ربات آماده کار است.")
        } else {
            btnStart.visibility = View.GONE
            appendLog("دسترسی محدود: لطفاً با حساب ادمین وارد شوید.")
        }
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            val u = etUsername.text.toString().trim()
            val p = etPassword.text.toString().trim()

            if (u == "admin" && (p == "mh1364" || p == "mh198553")) {
                prefs.SetUsername("admin")
                btnStart.visibility = View.VISIBLE
                btnStart.isEnabled = true
                appendLog("ورود ادمین با موفقیت انجام شد.")
                Toast.makeText(this, "ورود ادمین موفقیت‌آمیز بود", Toast.LENGTH_SHORT).show()
            } else {
                btnStart.visibility = View.GONE
                appendLog("خطا: نام کاربری یا رمز عبور ادمین اشتباه است!")
                Toast.makeText(this, "دسترسی غیرمجاز", Toast.LENGTH_SHORT).show()
            }
        }

        btnClear.setOnClickListener {
            logAdapter.clear()
        }

        btnStart.setOnClickListener {
            startBotDiagnostic()
        }

        btnSendEmail.setOnClickListener {
            sendEmail()
        }
    }

    private fun startBotDiagnostic() {
        if (isRunning) {
            Toast.makeText(this, "ربات در حال اجراست", Toast.LENGTH_SHORT).show()
            return
        }

        val token = prefs.GetToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "لطفا ابتدا وارد حساب خود شوید (توکن نامعتبر است)", Toast.LENGTH_SHORT).show()
            return
        }

        isRunning = true
        btnStart.isEnabled = false
        progressBar?.visibility = View.VISIBLE

        val activeBot = JomlakBot { logText: String ->
            lifecycleScope.launch(Dispatchers.Main) {
                appendLog(logText)
            }
        }
        bot = activeBot

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                // اگر از Intent دیتایی دریافت نشده بود، مستقیماً از سرور فچ کن
                if (receivedQuestions.isEmpty()) {
                    appendLog("🌐 در حال دریافت سوالات مستقیماً از سرور بازی...")
                    fetchQuestionsFromServer()
                }

                if (receivedQuestions.isEmpty()) {
                    appendLog("❌ سوالی برای تست یافت نشد (پاسخ سرور خالی بود یا ارتباط برقرار نشد).")
                    return@launch
                }

                val result: Boolean = withContext(Dispatchers.IO) {
                    activeBot.runOnGameDataWithResult(
                        ids = receivedIds,
                        questions = receivedQuestions
                    )
                }

                if (result) {
                    appendLog("🎉 عملیات عیب‌یابی با موفقیت کامل به پایان رسید.")
                } else {
                    appendLog("⚠️ عیب‌یابی به پایان رسید اما یک یا چند مرحله با خطا مواجه شدند.")
                }
            } catch (e: Exception) {
                appendLog("❌ خطا در اجرای ربات: ${e.localizedMessage}")
            } finally {
                isRunning = false
                progressBar?.visibility = View.GONE
                btnStart.isEnabled = true
            }
        }
    }

    private suspend fun fetchQuestionsFromServer() = withContext(Dispatchers.IO) {
        try {
            val api = RetrofitClient.getRetrofitInstance().create(ApiInterface::class.java)
            val lang = prefs.GetLang() ?: "fa"
            val token = App.Token + prefs.GetToken()

            // شناسه پایه پیش‌فرض یا فصل فعلی
            val currentId = 1
            val response = api.bases_with_token(token, lang, currentId).execute()

            if (response.isSuccessful && response.body() != null) {
                val levels = response.body()?.items ?: emptyList()
                val ids = arrayListOf<Int>()
                val questions = arrayListOf<String>()

                for (lvl in levels) {
                    ids.add(lvl.id)
                    // در دیتای بازی، کلمات سوال با دو نقطه یا مستقیم در title ذخیره می‌شوند
                    val qText = lvl.title ?: ""
                    questions.add(qText)
                }

                withContext(Dispatchers.Main) {
                    receivedIds = ids
                    receivedQuestions = questions
                    appendLog("✅ تعداد ${questions.size} مرحله با موفقیت از سرور بازی بارگذاری شد.")
                }
            } else {
                withContext(Dispatchers.Main) {
                    appendLog("❌ عدم دریافت داده از سرور (کد وضعیت: ${response.code()})")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                appendLog("❌ خطا در اتصال به سرور: ${e.localizedMessage}")
            }
        }
    }

    private fun sendEmail() {
        val emailUser = etEmailUser.text.toString().trim()
        val emailPass = etEmailPass.text.toString().trim()
        val emailTo = etEmailTo.text.toString().trim()

        if (emailUser.isEmpty() || emailPass.isEmpty() || emailTo.isEmpty()) {
            Toast.makeText(this, "لطفاً اطلاعات ایمیل را کامل وارد کنید", Toast.LENGTH_SHORT).show()
            return
        }

        val currentBot = bot
        if (currentBot == null) {
            Toast.makeText(this, "ابتدا باید ربات را حداقل یک‌بار اجرا کنید", Toast.LENGTH_SHORT).show()
            return
        }

        btnSendEmail.isEnabled = false
        progressBar?.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val success: Boolean = withContext(Dispatchers.IO) {
                    currentBot.sendEmailReport(emailUser, emailPass, emailTo)
                }

                if (success) {
                    Toast.makeText(this@BotActivity, "ایمیل با موفقیت ارسال شد", Toast.LENGTH_SHORT).show()
                    appendLog("گزارش با موفقیت به $emailTo ارسال شد.")
                } else {
                    Toast.makeText(this@BotActivity, "خطا در ارسال ایمیل", Toast.LENGTH_SHORT).show()
                    appendLog("ارسال ایمیل ناموفق بود.")
                }
            } catch (e: Exception) {
                appendLog("خطا در ارسال ایمیل: ${e.localizedMessage}")
                Toast.makeText(this@BotActivity, "خطا در ارسال ایمیل", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar?.visibility = View.GONE
                btnSendEmail.isEnabled = true
            }
        }
    }

    private fun appendLog(msg: String) {
        logAdapter.addLog(msg)
        val count = logAdapter.itemCount
        if (count > 0) {
            recyclerLogs.scrollToPosition(count - 1)
        }
    }
}
