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
            appendLog("ادمین شناسایی شد. پنل ربات آماده کار است.")
        } else {
            // اگر ادمین نباشد، دکمه اصلاً نشان داده نمی‌شود
            btnStart.visibility = View.GONE
            appendLog("دسترسی محدود: لطفاً با حساب ادمین وارد شوید.")
        }
    }

       private fun setupListeners() {
        // کلیک روی دکمه لاگین: اعتبارسنجی فیلدها و ارسال به متد handleLogin
        btnLogin.setOnClickListener {
            handleLogin()
        }

        // کلیک روی دکمه شروع ربات (تنها پس از ورود موفق فعال است)
        btnStart.setOnClickListener {
            startBotDiagnostic()
        }

        // کلیک روی دکمه ارسال گزارش ایمیل
        btnSendEmail.setOnClickListener {
            sendEmailReport()
        }
    }

    private fun handleLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim() // دریافت مستقیم پسورد از ورودی متن مانند نام کاربری

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "لطفاً نام کاربری و رمز عبور را وارد کنید", Toast.LENGTH_SHORT).show()
            return
        }


        // ساخت نمونه ربات با کانتکست و لامبدای لاگ
        bot = JomlakBot(
            context = this,
            onLog = { logText ->
                runOnUiThread {
                    appendLog(logText)
                }
            }
        )

        lifecycleScope.launch {
            btnLogin.isEnabled = false
            btnLogin.text = "در حال ورود..."
            appendLog("⏳ در حال تلاش برای احراز هویت با نام کاربری: $username")

            // فراخوانی متد login داخل JomlakBot در نخ IO
            val loginSuccess = withContext(Dispatchers.IO) {
                bot?.login(username, password) ?: false
            }

            btnLogin.isEnabled = true
            btnLogin.text = "ورود"

            if (loginSuccess) {
                appendLog("✅ احراز هویت موفقیت‌آمیز بود! توکن دریافت شد.")
                btnStart.isEnabled = true
                btnStart.text = "شروع ربات"
                Toast.makeText(this@BotActivity, "ورود با موفقیت انجام شد", Toast.LENGTH_SHORT).show()
            } else {
                appendLog("❌ ورود ناموفق بود. نام کاربری یا رمز عبور نامعتبر است.")
                btnStart.isEnabled = false
                Toast.makeText(this@BotActivity, "ورود ناموفق! اطلاعات را بررسی کنید", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startBotDiagnostic() {
        if (isRunning) return

        val currentBot = bot
        if (currentBot == null) {
            Toast.makeText(this, "لطفاً ابتدا لاگین کنید", Toast.LENGTH_SHORT).show()
            return
        }

        isRunning = true
        btnStart.isEnabled = false
        btnStart.text = "در حال اجرا..."

        lifecycleScope.launch {
            try {
                appendLog("🚀 ربات جملک شروع به اجرای عملیات کرد...")

                // اجرای عملیات کامل حل مراحل با run()
                val result: Boolean = withContext(Dispatchers.IO) {
                    try {
                        currentBot.run()
                        true
                    } catch (e: Exception) {
                        false
                    }
                }

                if (result) {
                    appendLog("🎉 پردازش تمام مراحل با موفقیت به پایان رسید.")
                } else {
                    appendLog("⚠️ عملیات ربات با خطا مواجه شد یا متوقف گردید.")
                }

            } catch (e: Exception) {
                appendLog("❌ خطای غیرمنتظره در اجرای ربات: ${e.message}")
            } finally {
                isRunning = false
                btnStart.isEnabled = true
                btnStart.text = "اجرای مجدد ربات"
            }
        }
    }


    private fun sendEmailReport() {
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
                // تایپ صریح Boolean برای withContext در ارسال ایمیل
                val success: Boolean = withContext(Dispatchers.IO) {
                    bot?.sendEmailReport(emailUser, emailPass, emailTo) ?: false
                }

                    if (success) {
                    Toast.makeText(this@BotActivity, "ایمیل با موفقیت ارسال شد", Toast.LENGTH_SHORT).show()
                    appendLog("گزارش با موفقیت به $emailTo ایمیل شد.")
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
