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
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerLogs: RecyclerView

    private lateinit var prefs: Prefs
    private val logList = mutableListOf<String>()
    private lateinit var logAdapter: LogAdapter
    private var bot: JomlakBot? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bot)

        prefs = Prefs(this)
        initViews()
        setupRecycler()
        setupListeners()

        // بررسی لاگین قبلی
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
        logAdapter = LogAdapter() // بدون هیچ آرگومان ورودی
        recyclerLogs.layoutManager = LinearLayoutManager(this)
        recyclerLogs.adapter = logAdapter
    }

    private fun checkAdminSession() {
        val currentUsername = prefs.GetUsername()
        if (currentUsername == "admin") {
            btnStart.isEnabled = true
            appendLog("ادمین شناسایی شد. پنل ربات آماده کار است.")
        } else {
            btnStart.isEnabled = false
            appendLog("لطفاً با حساب ادمین (admin / mh1364) وارد شوید.")
        }
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            val u = etUsername.text.toString().trim()
            val p = etPassword.text.toString().trim()

            // اعتبارسنجی انحصاری برای ادمین
            if (u == "admin" && p == "mh1364") {
                prefs.SetUsername("admin")
                btnStart.isEnabled = true
                appendLog("ورود ادمین با موفقیت انجام شد.")
                Toast.makeText(this, "ورود ادمین موفقیت‌آمیز بود", Toast.LENGTH_SHORT).show()
            } else {
                btnStart.isEnabled = false
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
        val token = prefs.GetToken() ?: ""
        btnStart.isEnabled = false
        progressBar.visibility = View.VISIBLE

        bot = JomlakBot { logText ->
            lifecycleScope.launch(Dispatchers.Main) {
                appendLog(logText)
            }
        }

        // دریافت لیست جملات منتقل‌شده از اینتنت بازی (یا لیست اشتراکی)
        val questions = intent.getStringArrayListExtra("EXTRA_QUESTIONS") ?: arrayListOf()
        val ids = intent.getIntegerArrayListExtra("EXTRA_IDS") ?: arrayListOf()

        lifecycleScope.launch {
            bot?.runOnGameData(ids, questions)
            progressBar.visibility = View.GONE
            btnStart.isEnabled = true
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

        btnSendEmail.isEnabled = false
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val success = bot?.sendEmailReport(emailUser, emailPass, emailTo) ?: false
            progressBar.visibility = View.GONE
            btnSendEmail.isEnabled = true

            if (success) {
                Toast.makeText(this@BotActivity, "ایمیل با موفقیت ارسال شد", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@BotActivity, "خطا در ارسال ایمیل", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun appendLog(msg: String) {
        logAdapter.addLog(msg)
        if (logAdapter.itemCount > 0) {
            recyclerLogs.scrollToPosition(logAdapter.itemCount - 1)
        }
    }
}
