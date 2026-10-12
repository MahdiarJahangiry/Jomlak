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
            // اگر ادمین نباشد، دکمه به کلی مخفی می‌شود
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

        val token = "dummy_token" // یا مقدار صحیح توکن پروژه شما

        isRunning = true
        btnStart.isEnabled = false
        progressBar?.visibility = View.VISIBLE

        // سازنده استاندارد JomlakBot به ترتیب: context, token, onLog
        val activeBot = JomlakBot(this, token) { logText ->
            lifecycleScope.launch(Dispatchers.Main) {
                appendLog(logText)
            }
        }
        bot = activeBot

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val result = withContext(Dispatchers.IO) {
                    activeBot.run()
                }

                if (result) {
                    appendLog("عملیات عیب‌یابی با موفقیت تکمیل شد.")
                } else {
                    appendLog("عیب‌یابی به پایان رسید یا با خطا متوقف شد.")
                }
            } catch (e: Exception) {
                appendLog("خطا در اجرای ربات: ${e.localizedMessage}")
            } finally {
                isRunning = false
                progressBar?.visibility = View.GONE
                btnStart.isEnabled = true
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
            Toast.makeText(this, "ابتدا باید ربات را اجرا کنید", Toast.LENGTH_SHORT).show()
            return
        }

        btnSendEmail.isEnabled = false
        progressBar?.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val success = withContext(Dispatchers.IO) {
                    currentBot.sendEmailReport(emailUser, emailPass, emailTo)
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
