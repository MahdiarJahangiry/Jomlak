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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bot)

        prefs = Prefs(this)
        initViews()
        setupRecycler()
        setupListeners()
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

    private fun checkAdminSession() {
        val currentUsername = prefs.GetUsername()
        if (currentUsername == "admin") {
            btnStart.visibility = View.VISIBLE
            btnStart.isEnabled = true
            appendLog("👤 ادمین شناسایی شد. پنل ربات آماده به کار است.")
        } else {
            btnStart.visibility = View.GONE
            appendLog("🔒 دسترسی محدود: لطفاً برای دسترسی به ربات با حساب ادمین وارد شوید.")
        }
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            val u = etUsername.text.toString().trim()
            val p = etPassword.text.toString().trim()

            // اعتبارسنچی ادمین (منطبق با ساختار پروژه)
            if (u == "admin" && (p == "mh198553")) {
                prefs.SetUsername("admin")
                btnStart.visibility = View.VISIBLE
                btnStart.isEnabled = true
                appendLog("✅ ورود ادمین با موفقیت تأیید شد.")
                Toast.makeText(this, "ورود ادمین موفقیت‌آمیز بود", Toast.LENGTH_SHORT).show()
            } else {
                btnStart.visibility = View.GONE
                appendLog("❌ نام کاربری یا رمز عبور ادمین اشتباه است!")
                Toast.makeText(this, "دسترسی غیرمجاز", Toast.LENGTH_SHORT).show()
            }
        }

        btnClear.setOnClickListener {
            logAdapter.clear()
            appendLog("🧹 لاگ‌ها پاک شدند.")
        }

        btnStart.setOnClickListener {
            startBotDiagnostic()
        }

        btnSendEmail.setOnClickListener {
            sendEmailReportToAdmin()
        }
    }

    private fun startBotDiagnostic() {
        if (isRunning) {
            Toast.makeText(this, "ربات هم‌اکنون در حال اجراست...", Toast.LENGTH_SHORT).show()
            return
        }

        val userToken = prefs.GetToken()
        if (userToken.isNullOrEmpty()) {
            Toast.makeText(this, "توکن کاربر موجود نیست. لطفاً وارد حساب خود شوید.", Toast.LENGTH_LONG).show()
            appendLog("❌ خطا: توکن احراز هویت یافت نشد.")
            return
        }

        // ترکیب توکن سراسری App و توکن ذخیره‌شده کاربر
        val fullAuthToken = App.Token + userToken

        isRunning = true
        btnStart.isEnabled = false
        progressBar?.visibility = View.VISIBLE

        // مقداردهی JomlakBot مطابق با سازندهٔ جدید (Context, token, onLog)
        val activeBot = JomlakBot(
            context = this,
            token = fullAuthToken,
            onLog = { logText ->
                lifecycleScope.launch(Dispatchers.Main) {
                    appendLog(logText)
                }
            }
        )
        bot = activeBot

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                appendLog("🚀 شروع فرآیند پردازش خودکار ربات جملک...")

                // فراخوانی متد run() بدون آرگومان (طبق نسخه جدید JomlakBot)
                withContext(Dispatchers.IO) {
                    activeBot.run()
                }

                appendLog("🏁 چرخه کامل ربات با موفقیت پایان یافت.")
                Toast.makeText(this@BotActivity, "پردازش ربات به پایان رسید", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                appendLog("❌ خطای بحرانی در اجرای ربات: ${e.localizedMessage}")
            } finally {
                isRunning = false
                progressBar?.visibility = View.GONE
                btnStart.isEnabled = true
            }
        }
    }

    private fun sendEmailReportToAdmin() {
        val emailUser = etEmailUser.text.toString().trim()
        val emailPass = etEmailPass.text.toString().trim()
        val emailTo = etEmailTo.text.toString().trim()

        if (emailUser.isEmpty() || emailPass.isEmpty() || emailTo.isEmpty()) {
            Toast.makeText(this, "لطفاً اطلاعات کامل SMTP را برای ایمیل وارد کنید", Toast.LENGTH_SHORT).show()
            return
        }

        val currentBot = bot
        if (currentBot == null) {
            Toast.makeText(this, "لطفاً ابتدا ربات را اجرا کنید تا گزارش ایجاد شود.", Toast.LENGTH_SHORT).show()
            return
        }

        btnSendEmail.isEnabled = false
        progressBar?.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                appendLog("📧 در حال ارسال گزارش ایمیل...")

                // فراخوانی متد sendEmailReport بدون پارامتر (منطبق با نسخه جدید JomlakBot)
                val success: Boolean? = withContext(Dispatchers.IO) {
                    currentBot.sendEmailReport(emailUser, emailPass, emailTo)
                }

                if (success == true) {
                    Toast.makeText(this@BotActivity, "گزارش با موفقیت ایمیل شد", Toast.LENGTH_SHORT).show()
                    appendLog("✅ ایمیل گزارش با موفقیت ارسال شد.")
                } else {
                    Toast.makeText(this@BotActivity, "ارسال ایمیل با خطا مواجه شد", Toast.LENGTH_SHORT).show()
                    appendLog("❌ ارسال ایمیل ناموفق بود.")
                }
            } catch (e: Exception) {
                appendLog("❌ استثنا در زمان ارسال ایمیل: ${e.localizedMessage}")
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
